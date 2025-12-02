package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase, Fill, Cat }
import chisel3.util
import chisel3.experimental.MultiIOModule


class InstructionDecode extends MultiIOModule {

  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val registerSetup = Input(new RegisterSetupSignals)
      val registerPeek  = Output(UInt(32.W))

      val testUpdates   = Output(new RegisterUpdates)
    })


  val io = IO(
    new Bundle {
      /**
        * TODO: Your code here.
        */

      val out = Output(new IDBarrierContent)
      val inIF = Input(new IFBarrierContent)
      val inWB = Input(new MEMBarrierContent)

      val WBDestReg = Output(UInt(32.W))
      val WBResult = Output(UInt(32.W))

      val WBLateDestReg = Output(UInt(32.W))
      val WBLateResult = Output(UInt(32.W))

      val WBLateLateDestReg = Output(UInt(32.W))
      val WBLateLateResult = Output(UInt(32.W))

      val freezeIn = Input(Bool())
      val freezeOut = Output(Bool())
    }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io


  /**
    * Setup. You should not change this code
    */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates


  /**
    * TODO: Your code here.
    */

  decoder.instruction := io.inIF.instruction
  
  
  
  registers.io.readAddress1 := decoder.instruction.registerRs1
  registers.io.readAddress2 := decoder.instruction.registerRs2
  registers.io.writeEnable  := io.inWB.controlSignals.regWrite
  registers.io.writeAddress := io.inWB.instruction.registerRd
  val decoder2   = Module(new Decoder).io
  decoder2.instruction := io.inWB.instruction

  
  val WBLateDestReg = RegInit(0.U(32.W))
  val WBLateResult = RegInit(0.U(32.W))

  val WBLateLateDestReg = RegInit(0.U(32.W))
  val WBLateLateResult = RegInit(0.U(32.W))

  WBLateDestReg := decoder2.instruction.registerRd

  when (decoder2.controlSignals.memRead === 1.U) {
    

    
    WBLateResult := io.inWB.dataOut
    registers.io.writeData    := io.inWB.dataOut

  }.elsewhen(decoder2.controlSignals.memWrite === 1.U) {
    WBLateDestReg := 0.U
    WBLateResult := 0.U
    registers.io.writeData    := io.inWB.ALUResult
  }.otherwise{
    registers.io.writeData    := io.inWB.ALUResult
    
    WBLateResult := io.inWB.ALUResult
  }

  WBLateLateDestReg := WBLateDestReg
  WBLateLateResult := WBLateResult

  io.WBLateLateDestReg := WBLateLateDestReg
  io.WBLateLateResult := WBLateLateResult

   io.WBLateDestReg := WBLateDestReg
  io.WBLateResult := WBLateResult

  val instruction   = decoder.instruction.instruction

  io.out.r2 := registers.io.readData2
  io.out.imm := registers.io.readData2
  when(decoder.op2Select === Op2Select.imm ) {
    when (decoder.immType === ImmFormat.ITYPE) {
      io.out.r2 := registers.io.readData2
      io.out.imm := decoder.instruction.immediateIType.asTypeOf(SInt(32.W)).asUInt

    }.elsewhen (decoder.immType === ImmFormat.STYPE) {
      io.out.r2 := registers.io.readData2
      io.out.imm := decoder.instruction.immediateSType.asTypeOf(SInt(32.W)).asUInt
      
    }.elsewhen (decoder.immType === ImmFormat.JTYPE) {
       io.out.r2 := registers.io.readData2.asTypeOf(SInt(32.W)).asUInt
       io.out.imm := decoder.instruction.immediateJType.asTypeOf(SInt(32.W)).asUInt

     }
    .elsewhen (decoder.immType === ImmFormat.BTYPE) {
      io.out.r2 := registers.io.readData2
      io.out.imm := decoder.instruction.immediateBType.asTypeOf(SInt(32.W)).asUInt
    
    }.elsewhen (decoder.immType === ImmFormat.UTYPE) {
      io.out.r2 := registers.io.readData2
      io.out.imm := decoder.instruction.immediateUType.asTypeOf(SInt(32.W)).asUInt
    }
  }

  io.out.instruction := io.inIF.instruction
  io.out.PC := io.inIF.PC
  io.out.controlSignals := decoder.controlSignals

  io.out.r1 := registers.io.readData1
  //io.readData2 := registers.io.readData2

  when (decoder2.controlSignals.memRead === 1.U) {
    io.WBDestReg := io.inWB.instruction.registerRd
  io.WBResult := io.inWB.dataOut //might be dataOut
  }.elsewhen(decoder2.controlSignals.memWrite === 1.U) {
    io.WBDestReg := 0.U
    io.WBResult := 0.U
  }.otherwise{
    io.WBDestReg := io.inWB.instruction.registerRd
    io.WBResult := io.inWB.ALUResult //might be dataOut
  }

  

  io.freezeOut := io.freezeIn
}
