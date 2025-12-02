
package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._

class MEMBarrier2 extends MultiIOModule{

   val io = IO(
    new Bundle {
      val inputInstruction = Input(new Instruction)
      val outputInstruction = Output(new Instruction)

      val PCInput = Input(UInt(32.W))
      val PCOutput = Output(UInt(32.W))

      val controlSignalsOutput = Output(new ControlSignals)
      val controlSignalsInput = Input(new ControlSignals)
    
      val ALUResultInput = Input(UInt(32.W))
      val ALUResultOutput = Output(UInt(32.W))
    
      val dataOutInput = Input(UInt(32.W))
      val dataOutOutput = Output(UInt(32.W))

    }
  )

  val PC   = RegInit(0.U(32.W)) 
  PC := io.PCInput + 4.U
  io.PCOutput := PC


  val decoder   = Module(new Decoder).io
  decoder.instruction := io.inputInstruction

  val memReadReg = RegInit(0.U(1.W))
  memReadReg := decoder.controlSignals.memRead
  
  when (memReadReg === 1.U) {
    printf("Vi er her")
    io.dataOutOutput := io.dataOutInput
    io.outputInstruction := io.inputInstruction
    io.controlSignalsOutput := io.controlSignalsInput
  }.otherwise {
    io.dataOutOutput := io.ALUResultInput
  }

  io.outputInstruction := io.inputInstruction
  io.controlSignalsOutput := io.controlSignalsInput
  //io.dataOutOutput := io.dataOutInput
  io.ALUResultOutput := io.ALUResultInput
  

  

}

