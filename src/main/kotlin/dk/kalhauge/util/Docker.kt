package dk.kalhauge.util

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import java.util.concurrent.TimeUnit.*
import com.github.dockerjava.core.DefaultDockerClientConfig

import com.github.dockerjava.core.DockerClientConfig

object Docker {
  val client: DockerClient

  init {
    val standard: DockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
    val httpClient = ApacheDockerHttpClient.Builder()
      .dockerHost(standard.dockerHost)
      .sslConfig(standard.sslConfig)
      .build()
    client = DockerClientBuilder.getInstance().withDockerHttpClient(httpClient).build()
    }

  fun pull(repository: String, timeout: Long = 30) : Image? =
    if (client.pullImageCmd(repository).exec(PullImageResultCallback()).awaitCompletion(timeout, SECONDS)) Image(client, repository)
    else null

  class Image(val client: DockerClient, val repository: String) {
    fun container(build: Container.() -> Unit = {}): Container =
      Container(this).also {
        it.build()
        }
      }

  class Container(val image: Image) : AutoCloseable {
      var id: String = ""
      val portBindings = mutableListOf<PortBinding>()
      val mountBindings = mutableListOf<Bind>()

    fun ports(vararg bindings: Pair<Int, Int>) {
      portBindings.addAll(bindings.map { PortBinding.parse("${it.first}:${it.second}") })
      }

    fun mounts(vararg bindings: Pair<String, String>) {
      mountBindings.addAll(bindings.map { Bind.parse("${it.first}:${it.second}") })
      }

    fun start() : Container {
      var config = HostConfig()
      if (mountBindings.isNotEmpty()) config = config.withBinds(mountBindings)
      if (portBindings.isNotEmpty()) config = config.withPortBindings(portBindings)
      val response = image.client.createContainerCmd(image.repository)
        .withHostConfig(config)
        .withEnv("""LANG="C.UTF-8"""")
        .exec()
      id = response.id
      image.client.startContainerCmd(response.id).exec()
      return this
      }

    fun stop() {
      //println("stopping $id ...")
      image.client.stopContainerCmd(id).exec()
      }

    override fun close() {
      //println("closing $id ...")
      if (image.client.inspectContainerCmd(id).exec().state.running ?: false) stop()
      image.client.removeContainerCmd(id).exec()
      }

    }

  }

