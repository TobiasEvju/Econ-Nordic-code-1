package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._

class IDBarrier2 extends MultiIOModule{

   val io = IO(
    new Bundle {
      val inputInstruction = Input(new Instruction)
      val outputInstruction = Output(new Instruction)

      val PCInput = Input(UInt(32.W))
      val PCOutput = Output(UInt(32.W))

      val controlSignalsOutput = Output(new ControlSignals)
      val controlSignalsInput = Input(new ControlSignals)

      val r1Input = Input(UInt(32.W))
      val r1Output = Output(UInt(32.W))

      val r2Input = Input(UInt(32.W))
      val r2Output = Output(UInt(32.W))

      val immInput = Input(UInt(12.W))
      val ImmOutput = Output(UInt(12.W))
    }
  )

  val PC   = RegInit(0.U(32.W)) 
  PC := io.PCInput + 4.U
  io.PCOutput := PC

  io.outputInstruction := io.inputInstruction
  io.controlSignalsOutput := io.controlSignalsInput
  io.r1Output := io.r1Input
  io.r2Output := io.r2Input
  io.ImmOutput := io.immInput

}
