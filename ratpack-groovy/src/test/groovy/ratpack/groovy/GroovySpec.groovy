/*
 * Copyright 2014 the original author or authors.
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

package ratpack.groovy

import ratpack.error.ServerErrorHandler
import ratpack.error.internal.DefaultDevelopmentErrorHandler
import ratpack.func.Action
import ratpack.handling.Chain
import ratpack.impose.ImpositionsSpec
import ratpack.impose.ServerConfigImposition
import ratpack.server.RatpackServer
import ratpack.server.ServerConfig
import ratpack.test.ServerBackedApplicationUnderTest
import ratpack.test.internal.RatpackGroovyDslSpec

class GroovySpec extends RatpackGroovyDslSpec {

  def "can use chain method to wrap chain"() {
    when:
    handlers {
      all chain(new Action<Chain>() {
        @Override
        void execute(Chain thing) throws Exception {
          Groovy.chain(thing) {
            get("foo") { render "bar" }
          }
        }
      })
    }

    then:
    getText("foo") == "bar"
  }

  def "can use chain method to create action"() {
    when:
    handlers {
      all chain(Groovy.chain {
        get("foo") { render "bar" }
      })
    }

    then:
    getText("foo") == "bar"
  }

  class MyHandlers implements Action<Chain> {
    @Override
    void execute(Chain chain) throws Exception {
      Groovy.chain(chain) {
        get { // if this line moves, the test below will start failing
          // no response
        }
      }
    }
  }

  def "dangling closure handler is reported"() {
    given:
    serverConfig {
      development(true)
    }

    bindings {
      bindInstance ServerErrorHandler, new DefaultDevelopmentErrorHandler()
    }

    when:
    handlers {
      all chain(new MyHandlers())
    }

    then:
    text == "No response sent for GET request to / (last handler: closure at line 65 of GroovySpec.groovy)"
  }

  def "Supports ServerConfigImposition with GString"() {
    setup:
    def testServer = new ServerBackedApplicationUnderTest() {
      @Override
      protected RatpackServer createServer() throws Exception {
        return RatpackServer.of { s ->
          s.serverConfig(ServerConfig.of { serverConfigBuilder ->
            serverConfigBuilder.props([color: 'green'])
          })
            .handlers({ Chain chain ->
            chain.get('color', { ctx -> ctx.render(ctx.getServerConfig().get('/color', String)) })
          })
        }
      }

      @Override
      protected void addImpositions(ImpositionsSpec impositions) {
        String blue = 'blue'
        impositions.add(ServerConfigImposition.of({ serverConfig ->
          serverConfig.props([
            color: "${blue}"
          ])
        }))
      }
    }

    expect:
    testServer.test {
      httpClient ->
        assert httpClient.get('color').body.text == 'blue'
    }

  }

}
