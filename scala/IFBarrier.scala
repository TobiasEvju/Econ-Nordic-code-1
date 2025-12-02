package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._

class IFBarrier2 extends MultiIOModule{

   val io = IO(
    new Bundle {
      val inputInstruction = Input(new Instruction)
      val outputInstruction = Output(new Instruction)
      val PCInput = Input(UInt(32.W))
      val PCOutput = Output(UInt(32.W))
    }
  )

  val PC   = RegInit(UInt(32.W), 0.U)
  PC := io.PCInput + 4.U
  io.PCOutput := PC

  io.outputInstruction := io.inputInstruction

  print(io.PCOutput)

}
