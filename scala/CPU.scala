package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._


class CPU extends MultiIOModule {

  val testHarness = IO(
    new Bundle {
      val setupSignals = Input(new SetupSignals)
      val testReadouts = Output(new TestReadouts)
      val regUpdates   = Output(new RegisterUpdates)
      val memUpdates   = Output(new MemUpdates)
      val currentPC    = Output(UInt(32.W))
    }
  )

  /**
    You need to create the classes for these yourself
    */
  val IFBarrier  = Module(new IFBarrier).io
  val IDBarrier  = Module(new IDBarrier).io
  val EXBarrier  = Module(new EXBarrier).io
  val MEMBarrier = Module(new MEMBarrier).io
  //val WBBarrier = Module(new WBBarrier).io

  val ID  = Module(new InstructionDecode)
  val IF  = Module(new InstructionFetch)
  val EX  = Module(new Execute)
  val MEM = Module(new MemoryFetch)
  // val WB  = Module(new Execute) (You may not need this one?)


  /**
    * Setup. You should not change this code
    */
  IF.testHarness.IMEMsetup     := testHarness.setupSignals.IMEMsignals
  ID.testHarness.registerSetup := testHarness.setupSignals.registerSignals
  MEM.testHarness.DMEMsetup    := testHarness.setupSignals.DMEMsignals

  testHarness.testReadouts.registerRead := ID.testHarness.registerPeek
  testHarness.testReadouts.DMEMread     := MEM.testHarness.DMEMpeek

  /**
    spying stuff
    */
  testHarness.regUpdates := ID.testHarness.testUpdates
  testHarness.memUpdates := MEM.testHarness.testUpdates
  testHarness.currentPC  := IF.testHarness.PC


  /**
    TODO: Your code here
    */

    IFBarrier.in <> IF.io.out
    IDBarrier.in <> ID.io.out
    EXBarrier.in <> EX.io.out
    MEMBarrier.in <> MEM.io.out

    ID.io.inIF <> IFBarrier.out
    EX.io.in <> IDBarrier.out
    MEM.io.in <> EXBarrier.out
    ID.io.inWB <> MEMBarrier.out

    IF.io.jump := MEM.io.jumpOut
    IF.io.targetPC := MEM.io.targetPCOut

    //flush
    IDBarrier.flush := IF.io.flush
    IFBarrier.flush := IF.io.flush
    EXBarrier. flush := IF.io.flush

    //FOrwarding
    EX.io.MEMDestReg := MEM.io.MEMDestReg
    EX.io.MEMResult := MEM.io.MEMResult
    EX.io.MEMLoad := MEM.io.MEMLoad

    EX.io.WBDestReg := ID.io.WBDestReg
    EX.io.WBResult := ID.io.WBResult

    EX.io.WBLateDestReg := ID.io.WBLateDestReg
    EX.io.WBLateResult := ID.io.WBLateResult

    EX.io.WBLateLateDestReg := ID.io.WBLateLateDestReg
    EX.io.WBLateLateResult := ID.io.WBLateLateResult

    IF.io.freezeIn := false.B
    IFBarrier.freezeIn := false.B
    ID.io.freezeIn := false.B
    IDBarrier.freezeIn := false.B
    

    EXBarrier.freezeIn := EX.io.freezeOut
    IF.io.freezeIn := EXBarrier.freezeOut
    IFBarrier.freezeIn := IF.io.freezeOut
    ID.io.freezeIn := IFBarrier.freezeOut
    IDBarrier.freezeIn := ID.io.freezeOut
    


}
