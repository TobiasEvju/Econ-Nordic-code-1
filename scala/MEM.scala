package FiveStage
import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule


class MemoryFetch() extends MultiIOModule {


  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val DMEMsetup      = Input(new DMEMsetupSignals)
      val DMEMpeek       = Output(UInt(32.W))

      val testUpdates    = Output(new MemUpdates)
    })

  val io = IO(
    new Bundle {


      val in = Input(new EXBarrierContent)
      val out = Output(new MEMBarrierContent)

      val MEMDestReg = Output(UInt(32.W))
      val MEMResult = Output(UInt(32.W))
      val MEMLoad = Output(Bool())

      val jumpOut = Output(Bool())
      val targetPCOut = Output(UInt(32.W))
    })


  val DMEM = Module(new DMEM)


  /**
    * Setup. You should not change this code
    */
  DMEM.testHarness.setup  := testHarness.DMEMsetup
  testHarness.DMEMpeek    := DMEM.io.dataOut
  testHarness.testUpdates := DMEM.testHarness.testUpdates


  /**
    * Your code here.
    */
  DMEM.io.dataIn      := io.in.r2
  DMEM.io.dataAddress := io.in.ALUResult
  DMEM.io.writeEnable := io.in.controlSignals.memWrite

  io.out.dataOut := DMEM.io.dataOut

  io.out.PC := io.in.PC
  io.out.instruction := io.in.instruction
  io.out.controlSignals := io.in.controlSignals
  io.out.ALUResult := io.in.ALUResult

  io.targetPCOut := io.in.targetPC
  io.jumpOut := io.in.jump

  
  io.MEMDestReg := io.in.instruction.registerRd
  

  val decoder   = Module(new Decoder).io
  decoder.instruction := io.in.instruction

  val memReadReg = RegInit(0.U(1.W))
  memReadReg := decoder.controlSignals.memRead  

  when (decoder.controlSignals.memRead === 1.U) {
    io.MEMLoad := true.B
    io.MEMResult := DMEM.io.dataOut

    //when (memReadReg === 1.U){
    //  memReadReg := 0.U
    //  io.MEMLoad := false.B
    //}
  }.otherwise{
    io.MEMResult := io.in.ALUResult
    io.MEMLoad := false.B
  }

  when(decoder.controlSignals.memWrite === 1.U) {
    io.MEMDestReg := 0.U
    io.MEMResult := 0.U
  }
  
  
}
