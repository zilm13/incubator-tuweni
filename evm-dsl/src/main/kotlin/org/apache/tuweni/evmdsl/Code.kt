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
package org.apache.tuweni.evmdsl

import org.apache.tuweni.bytes.Bytes

/**
 * EVM code represented as a set of domain-specific instructions.
 *
 * The code can be serialized into bytes to be deployed on a EVM-compatible chain.
 *
 * The code can also read bytecode and represent it in this DSL.
 */
class Code(val instructions: List<Instruction>) {

  companion object {
    fun read(codeBytes: Bytes): Code {
      return Code(
        buildList {
          var index = 0

          while (index < codeBytes.size()) {
            val model = InstructionRegistry.opcodes.get(codeBytes.get(index))
            if (model == null) {
              throw IllegalArgumentException("Unknown opcode " + Bytes.of(codeBytes.get(index)) + " at index " + index)
            }
            index++
            this.add(model.creator(codeBytes, index))
            index += model.additionalBytesToRead
          }
        }
      )
    }
  }

  fun validate(): CodeValidationError? {
    var stackSize = 0
    val visited = mutableSetOf<Int>()
    var index = 0
    while (visited.add(index)) {
      val currentInstruction = instructions.getOrNull(index) ?: break
      if (currentInstruction.stackItemsNeeded() > stackSize) {
        return CodeValidationError(currentInstruction, index, Error.STACK_UNDERFLOW)
      }
      stackSize += currentInstruction.stackItemsProduced()
      if (stackSize > 1024) {
        return CodeValidationError(currentInstruction, index, Error.STACK_OVERFLOW)
      }
      if (currentInstruction is Invalid) {
        return CodeValidationError(currentInstruction, index, Error.HIT_INVALID_OPCODE)
      }
      // TODO cannot follow jumps right now.
      if (currentInstruction == Jump || currentInstruction == Jumpi) {
        break
      }

      index++
    }
    return null
  }

  fun toBytes(): Bytes {
    return Bytes.wrap(instructions.map { it.toBytes() })
  }

  override fun toString(): String {
    return instructions.map { it.toString() }.joinToString("\n")
  }
}

enum class Error {
  STACK_UNDERFLOW,
  STACK_OVERFLOW,
  HIT_INVALID_OPCODE
}

data class CodeValidationError(val instruction: Instruction, val index: Int, val error: Error)
