/*
The MIT License (MIT)

Copyright (c) 2014 Steve Wagner (aka ciroque)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package org.ciroque.securesocial.mongodb.userservice

import reactivemongo.bson._
import org.joda.time.DateTime
import reactivemongo.bson.BSONDateTime
import securesocial.core.providers.Token
import scala.Some
import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration.Duration
import org.ciroque.coredata.{DataStoreFailureResult, DataStoreSuccessWithProductResult, DataStoreResult}

class MongoUserDataStore(properties: UserDataStoreProperties) extends UserDataStore {

  private val USER_NOT_FOUND = USER_NOT_FOUND

  /* *******************************************************************************************************************
      Serializers
   */

  implicit object BSONDateTimeReader extends reactivemongo.bson.BSONReader[reactivemongo.bson.BSONValue, org.joda.time.DateTime] {
    def read(dateTime: BSONValue) = {
      dateTime match {
        case dt: BSONDateTime => new DateTime(dt.value)
        case _ => new DateTime()
      }
    }
  }

  implicit object JodaDateTimeWriter extends reactivemongo.bson.BSONWriter[org.joda.time.DateTime, reactivemongo.bson.BSONValue] {
    def write(dateTime: DateTime) = BSONDateTime(dateTime.getMillis)
  }

  implicit object SecureSocialTokenBSONWriter extends BSONDocumentWriter[securesocial.core.providers.Token] {
    def write(token: Token) = {
      BSONDocument(
        "uuid" -> token.uuid,
        "email" -> token.email,
        "creationTime" -> token.creationTime,
        "expirationTime" -> token.expirationTime,
        "isSignUp" -> token.isSignUp
      )
    }
  }

  implicit object SecureSocialTokenBSONReader extends BSONDocumentReader[Token] {
    def read(doc: BSONDocument): Token = {
      Token(
        doc.getAs[String]("uuid").get,
        doc.getAs[String]("email").get,
        doc.getAs[DateTime]("creationTime").get,
        doc.getAs[DateTime]("expirationTime").get,
        doc.getAs[Boolean]("isSignUp").get
      )
    }
  }

  /* *******************************************************************************************************************
      Privates
   */

  private lazy val localDatabase = {
    val driver = new MongoDriver()
    val connection = driver.connection(List(properties.hostname))
    connection(properties.databaseName)
  }

  private lazy val userCollection: BSONCollection = {
    localDatabase(properties.userCollectionName)
  }

  private lazy val tokenCollection: BSONCollection = {
    localDatabase(properties.tokenCollectionName)
  }

  def findUserByEmailAndProvider(email: String, providerId: String): DataStoreResult = {
    val query = BSONDocument("email" -> email, "providerId" -> providerId)
    val cursor = userCollection.find(query).cursor[User]
    val r: Future[DataStoreResult] = for {
      firstUser: Option[User] <- cursor.headOption
    } yield {
      firstUser
        .map {
        user => DataStoreSuccessWithProductResult(Some(user))
      }
        .getOrElse(DataStoreFailureResult(404, USER_NOT_FOUND))
    }

    Await.result(r, Duration(10, "seconds"))
  }

  def findUserByIdAndProvider(id: String, providerId: String): DataStoreResult = {
    val query = BSONDocument("userId" -> id, "providerId" -> providerId)
    val cursor = userCollection.find(query).cursor[User]
    val result: Future[DataStoreResult] = for {
      firstUser: Option[User] <- cursor.headOption
    } yield {
      firstUser
        .map {
        user => DataStoreSuccessWithProductResult(Some(user))
      }
        .getOrElse(DataStoreFailureResult(404, USER_NOT_FOUND))
    }

    Await.result(result, Duration(10, "seconds"))
  }

  def saveUser(user: User): DataStoreResult = {
    user.pid match {
      case None =>
        userCollection.insert(user)
      case Some(pid) =>
        val query = BSONDocument("_id" -> pid)
        userCollection.update(query, user)
    }

    DataStoreSuccessWithProductResult(Some(user))
  }

  def deleteExpiredTokens: DataStoreResult = {
    var query = BSONDocument("expirationTime" -> BSONDocument("$lte" -> BSONDateTime(DateTime.now().getMillis)))
    tokenCollection.find(query).cursor[Token].enumerate().map(t => deleteToken(t.uuid))
    DataStoreSuccessWithProductResult(None)
  }

  def deleteToken(uuid: String): DataStoreResult = {
    val query = BSONDocument("uuid" -> uuid)
    val le = Await.result(tokenCollection.remove(query), Duration(10, "seconds"))
    if (le.ok) {
      DataStoreSuccessWithProductResult(Some(uuid))
    } else {
      val errMsg = le.errMsg match {
        case Some(msg) => msg
        case None => le.message
      }

      DataStoreFailureResult(le.code.getOrElse(-1), errMsg, Some(le.getCause))
    }
  }

  def findToken(uuid: String): DataStoreResult = {
    val query = BSONDocument("uuid" -> uuid)
    val cursor = tokenCollection.find(query).cursor[Token]
    val result = for {
      firstToken: Option[Token] <- cursor.headOption(ExecutionContext.global)
    } yield {
      firstToken
        .map(token => DataStoreSuccessWithProductResult(Some(token)))
        .getOrElse(DataStoreFailureResult(404, "Token Not Found"))
    }

    Await.result(result, Duration(10, "seconds"))
  }

  def saveToken(token: Token): DataStoreResult = {
    tokenCollection.insert(token)
    DataStoreSuccessWithProductResult(Some(token.uuid))
  }
}
