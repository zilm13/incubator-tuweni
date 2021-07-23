/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tuweni.jsonrpc.app

import org.apache.tuweni.config.Configuration
import org.apache.tuweni.config.PropertyValidator
import org.apache.tuweni.config.SchemaBuilder
import java.nio.file.Path

/**
 * Configuration of the JSON-RPC server as a TOML-based file.
 */
class JSONRPCConfig(val filePath: Path) {

  companion object {

    fun schema() = SchemaBuilder.create()
      .addInteger("numberOfThreads", 10, "Number of threads for each thread pool", null)
      .addInteger("metricsPort", 9090, "Metric service port", PropertyValidator.isValidPort())
      .addString("metricsNetworkInterface", "localhost", "Metric service network interface", null)
      .addBoolean("metricsGrpcPushEnabled", false, "Enable pushing metrics to gRPC service", null)
      .addBoolean("metricsPrometheusEnabled", false, "Enable exposing metrics on the Prometheus endpoint", null)
      .addInteger("port", 8845, "JSON-RPC server port", PropertyValidator.isValidPort())
      .addString("networkInterface", "127.0.0.1", "JSON-RPC server network interface", null)
      .toSchema()
  }

  val config = Configuration.fromToml(filePath, schema())

  fun numberOfThreads() = config.getInteger("numberOfThreads")
  fun metricsPort() = config.getInteger("metricsPort")
  fun metricsNetworkInterface() = config.getString("metricsNetworkInterface")
  fun metricsGrpcPushEnabled() = config.getBoolean("metricsGrpcPushEnabled")
  fun metricsPrometheusEnabled() = config.getBoolean("metricsPrometheusEnabled")

  fun port() = config.getInteger("port")
  fun networkInterface() = config.getString("networkInterface")
}