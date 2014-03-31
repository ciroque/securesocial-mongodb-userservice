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

import play.api.Play

class MongoDataStoreProperties extends UserDataStoreProperties {
  override val hostname: String = Play.current.configuration.getString("mongo.hostname").getOrElse("localhost")
  override val databaseName: String = Play.current.configuration.getString("mongo.database").getOrElse("securesocial")
  override val userCollectionName: String = Play.current.configuration.getString("mongo.userCollection").getOrElse("users")
  override val tokenCollectionName: String = Play.current.configuration.getString("mongo.tokenCollection").getOrElse("tokens")
  override val username: String = Play.current.configuration.getString("mongo.username").getOrElse(null)
  override val password: String = Play.current.configuration.getString("mongo.password").getOrElse(null)
}