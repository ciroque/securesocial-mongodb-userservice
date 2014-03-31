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

import securesocial.core.Identity
import securesocial.core.providers.Token
import org.ciroque.coredata.{DataStoreFailureResult, DataStoreSuccessWithProductResult}

class MongoUserService(application: play.Application) extends securesocial.core.UserService {

  private val userDataStore: MongoUserDataStore = new MongoUserDataStore(new MongoDataStoreProperties)

  /**
   * Finds a user that matches the specified id
   *
   * @param id the user id
   * @return an optional user
   */
  def find(id: securesocial.core.IdentityId): Option[Identity] = {
    val result = userDataStore.findUserByIdAndProvider(id.userId, id.providerId) match {
      case DataStoreSuccessWithProductResult(identity: Identity) => Some(identity)
      case DataStoreFailureResult(_, msg, None) => None
    }
    result
  }

  /**
   * Finds a user by email and provider id.
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation.
   *
   * @param email - the user email
   * @param providerId - the provider id
   * @return
   */
  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    val result = userDataStore.findUserByEmailAndProvider(email, providerId) match {
      case DataStoreSuccessWithProductResult(identity: Identity) => Some(identity)
      case DataStoreFailureResult(_, msg, thrown) =>
        None
    }
    result
  }

  /**
   * Saves the user.  This method gets called when a user logs in.
   * This is your chance to save the user information in your backing store.
   * @param user
   */
  def save(user: Identity): Identity = {
    val result = userDataStore.saveUser(User.fromIdentity(user)) match {
      case DataStoreSuccessWithProductResult(identity: Identity) => identity
      case DataStoreFailureResult(_, msg, thrown) => user
    }
    result
  }

  /**
   * Saves a token.  This is needed for users that
   * are creating an account in the system instead of using one in a 3rd party system.
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param token The token to save
   * @return A string with a uuid that will be embedded in the welcome email.
   */
  def save(token: Token) = {
    userDataStore.saveToken(token) match {
      case DataStoreSuccessWithProductResult(uuid: String) => uuid
      case DataStoreFailureResult(_, msg, thrown) => msg
    }
  }


  /**
   * Finds a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param token the token id
   * @return
   */
  def findToken(token: String): Option[Token] = {
    val result = userDataStore.findToken(token) match {
      case DataStoreSuccessWithProductResult(token: Token) => Some(token)
      case DataStoreFailureResult(_, msg, thrown) => None
    }
    result
  }

  /**
   * Deletes a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param uuid the token id
   */
  def deleteToken(uuid: String) {
    userDataStore.deleteToken(uuid)
  }

  /**
   * Deletes all expired tokens
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   */
  def deleteExpiredTokens() {
    userDataStore.deleteExpiredTokens
  }
}
