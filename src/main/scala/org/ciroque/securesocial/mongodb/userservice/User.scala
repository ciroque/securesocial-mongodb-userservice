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

import securesocial.core._
import play.api.libs.Codecs
import reactivemongo.bson._
import securesocial.core.OAuth2Info
import securesocial.core.OAuth1Info
import securesocial.core.IdentityId
import securesocial.core.PasswordInfo

case class User(pid: Option[BSONObjectID] = None,
                userId: String,
                providerId: String,
                email: Option[String],
                firstName: String,
                lastName: String,
                authMethod: AuthenticationMethod,
                oAuth1Info: Option[OAuth1Info] = None,
                oAuth2Info: Option[OAuth2Info] = None,
                passwordInfo: Option[PasswordInfo] = None)
  extends Identity {
  def identityId: IdentityId = IdentityId(userId, providerId)

  def fullName: String = s"$firstName $lastName"

  def avatarUrl: Option[String] = email.map {
    e => s"http://www.gravatar.com/avatar/${Codecs.md5(e.getBytes)}.png"
  }
}

object User {

  /* *******************************************************************************************************************
  AuthenticationMethod serialization
  */

  implicit object AuthenticationMethodBSONWriter extends BSONWriter[securesocial.core.AuthenticationMethod, BSONValue] {
    def write(authMethod: AuthenticationMethod) = BSONDocument("authMethod" -> authMethod.method)
  }

  implicit object AuthenticationMethodBSONReader extends BSONReader[BSONValue, securesocial.core.AuthenticationMethod] {
    def read(value: BSONValue) = {
      value match {
        case s: BSONString => securesocial.core.AuthenticationMethod(s.value)
        case _ => securesocial.core.AuthenticationMethod("")
      }
    }
  }

  /* *******************************************************************************************************************
  OAuth1Info serialization
  */

  implicit object OAuth1InfoBSONWriter extends BSONWriter[securesocial.core.OAuth1Info, BSONValue] {
    def write(oauth: OAuth1Info) = BSONDocument("secret" -> oauth.secret, "token" -> oauth.token)
  }

  implicit object OAuth1InfoBSONReader extends BSONReader[BSONValue, securesocial.core.OAuth1Info] {
    def read(value: BSONValue) = {
      value match {
        case doc: BSONDocument => OAuth1Info(doc.getAs[String]("token").get, doc.getAs[String]("secret").get)
        case _ => OAuth1Info("", "")
      }
    }
  }

  /* *******************************************************************************************************************
  OAuth2Info serialization
  */

  implicit object OAuth2InfoBSONWriter extends BSONWriter[securesocial.core.OAuth2Info, BSONValue] {
    def write(oauth: OAuth2Info) = BSONDocument(
      "accessToken" -> oauth.accessToken,
      "tokenType" -> oauth.tokenType,
      "expiresIn" -> oauth.expiresIn,
      "refreshToken" -> oauth.refreshToken)
  }

  implicit object OAuth2InfoBSONReader extends BSONReader[BSONValue, securesocial.core.OAuth2Info] {
    def read(value: BSONValue) = {
      value match {
        case doc: BSONDocument => OAuth2Info(
          doc.getAs[String]("accessToken").get,
          doc.getAs[String]("tokenType"),
          doc.getAs[Int]("expiresIn"),
          doc.getAs[String]("refreshToken"))
        case _ => OAuth2Info("", Option(""))
      }
    }
  }

  /* *******************************************************************************************************************
  PasswordInfo serialization
  */

  implicit object PasswordInfoBSONWriter extends BSONWriter[securesocial.core.PasswordInfo, BSONValue] {
    def write(pi: PasswordInfo) = BSONDocument(
      "hasher" -> pi.hasher,
      "password" -> pi.password,
      "salt" -> pi.salt)
  }

  implicit object PasswordInfoBSONReader extends BSONReader[BSONValue, securesocial.core.PasswordInfo] {
    def read(value: BSONValue) = {
      value match {
        case doc: BSONDocument => PasswordInfo(
          doc.getAs[String]("hasher").get,
          doc.getAs[String]("password").get,
          doc.getAs[String]("salt"))
        case _ => PasswordInfo("", "")
      }
    }
  }

  implicit object UserBSONWriter extends BSONDocumentWriter[User] {
    def write(user: User) = {
      BSONDocument(
        "_id" -> user.pid,
        "userId" -> user.identityId.userId,
        "providerId" -> user.identityId.providerId,
        "email" -> user.email,
        "firstName" -> user.firstName,
        "lastName" -> user.lastName,
        "authMethod" -> user.authMethod,
        "oAuth1Info" -> user.oAuth1Info,
        "oAuth2Info" -> user.oAuth2Info,
        "passwordInfo" -> user.passwordInfo
      )
    }
  }


  implicit object UserBSONReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      User(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("userId").get,
        doc.getAs[String]("providerId").get,
        doc.getAs[String]("email"),
        doc.getAs[String]("firstName").get,
        doc.getAs[String]("lastName").get,
        doc.getAs[AuthenticationMethod]("authMethod").get,
        doc.getAs[OAuth1Info]("oAuth1Info"),
        doc.getAs[OAuth2Info]("oAuth2Info"),
        doc.getAs[PasswordInfo]("passwordInfo")
      )
    }
  }

  def fromIdentity(user: Identity) = {
    User(
      pid = None,
      userId = user.identityId.userId,
      providerId = user.identityId.providerId,
      email = user.email,
      firstName = user.firstName,
      lastName = user.lastName,
      authMethod = user.authMethod,
      oAuth1Info = user.oAuth1Info,
      oAuth2Info = user.oAuth2Info,
      passwordInfo = user.passwordInfo
    )
  }
}
