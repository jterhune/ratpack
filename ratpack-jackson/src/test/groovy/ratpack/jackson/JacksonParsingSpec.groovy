/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.jackson

import ratpack.test.internal.RatpackGroovyDslSpec

import spock.lang.Unroll

import static Jackson.jsonNode
import static Jackson.fromJson

class JacksonParsingSpec extends RatpackGroovyDslSpec {

  def setup() {
    modules << new JacksonModule()
  }

  def "can parse json node"() {
    when:
    handlers {
      post {
        def node = parse jsonNode()
        response.send node.get("value").toString()
      }
    }

    and:
    request.contentType("application/json").body([value: 3])

    then:
    postText() == "3"
  }

  @Unroll("can parse json as #classType")
  def "can parse json as object"() {
    when:
    handlers {
      post {
        def object = parse fromJson(classType)
        response.send "${object.value}:${object.foo?.value}"
      }

    }

    and:
    request.contentType("application/json").body([value: 1, foo: [value: 2]])

    then:
    postText() == "1:2"

    where:
    classType   | _
    HashMap     | _
    Map         | _
    Object      | _
    Pogo        | _
  }


}
