
package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._
import chisel3.util.{ BitPat, Cat }

class IFBarrierContent extends Bundle {
    val instruction = new Instruction
    val PC = UInt(32.W)
}

class IFBarrier extends MultiIOModule{
   val io = IO(
    new Bundle {
        val in = Input(new IFBarrierContent)
        val out = Output(new IFBarrierContent)
        val freezeIn = Input(Bool())
        val freezeOut = Output(Bool())

        val flush = Input(Bool())
    }
  )
  
  val PC = RegInit(UInt(32.W), 0.U)
  val freeze_inst = Reg(new Instruction)
  val freeze_next = Reg(Bool())

  val flush_inst = Reg(new Instruction)
  val flush_next = Reg(Bool())

  freeze_next := io.freezeIn
  
  io.out.PC := PC


  val w = Wire(new Instruction)
  w.instruction := 0.U

  when(!io.freezeIn){
    PC := io.in.PC
  }.otherwise{
    PC := PC
  }

  when (!freeze_next) {
  io.out.instruction := io.in.instruction
  freeze_inst := io.in.instruction
  }.otherwise {
    io.out.instruction := freeze_inst
  }

  flush_next := io.flush

  when (!flush_next) {
  flush_inst := w
  }.otherwise {
    io.out.PC := 0.U
    io.out.instruction := flush_inst
  }

  io.freezeOut := io.freezeIn
}

class IDBarrierContent extends Bundle {
    val instruction = new Instruction
    val PC = UInt(32.W)
    val controlSignals = new ControlSignals
    val r1 = UInt(32.W)
    val r2 = UInt(32.W)
    val imm = UInt(32.W)
    
}

class IDBarrier extends MultiIOModule{
   val io = IO(
    new Bundle {
        val in = Input(new IDBarrierContent)
        val out = Output(new IDBarrierContent)
        val freezeIn = Input(Bool())

        val flush = Input(Bool())
        
    }
  )
  
  val regs = Reg(new IDBarrierContent)
  
  val nop = Reg(new IDBarrierContent)

  io.out := regs

  val controlSignals = Wire(new ControlSignals)
  val w = Wire(new Instruction)
  w.instruction := 0.U
  controlSignals.regWrite   := false.B
    controlSignals.memRead    := false.B
    controlSignals.memWrite   := false.B
    controlSignals.branch     := false.B
    controlSignals.jump       := false.B
    nop.instruction := w
    nop.PC := 0.U
    nop.controlSignals := controlSignals
    nop.r2 := 0.U
    nop.r1 := 0.U 
    nop.imm := 0.U 
    

  when (!io.freezeIn) {
  regs := io.in
  
  }.otherwise {
    regs := regs
    
  }

  when (io.flush) {
    regs := nop
  }
  
}

class EXBarrierContent extends Bundle {
    val instruction = new Instruction
    val PC = UInt(32.W)
    val controlSignals = new ControlSignals
    val r2 = UInt(32.W)
    val ALUResult = UInt(32.W)
    val targetPC = UInt(32.W)
    val jump = Bool()
}

class EXBarrier extends MultiIOModule{
   val io = IO(
    new Bundle {
        val in = Input(new EXBarrierContent)
        val out = Output(new EXBarrierContent)
        val freezeIn = Input(Bool())
        val freezeOut = Output(Bool())

        val flush = Input(Bool())
    }
  )
  
  val regs = Reg(new EXBarrierContent)
  val nop = Reg(new EXBarrierContent)

  io.out := regs

  val controlSignals = Wire(new ControlSignals)
  val w = Wire(new Instruction)
  w.instruction := 0.U
  controlSignals.regWrite   := false.B
    controlSignals.memRead    := false.B
    controlSignals.memWrite   := false.B
    controlSignals.branch     := false.B
    controlSignals.jump       := false.B
    nop.instruction := w
    nop.PC := 0.U
    nop.controlSignals := controlSignals
    nop.r2 := 0.U
    nop.ALUResult := 0.U 
    nop.targetPC := 0.U 
    nop.jump := false.B

  when (!io.freezeIn) {
  regs := io.in
  //io.out.instruction := io.in.instruction
  }.otherwise {
    regs := nop
  }

  when (io.flush) {
    regs := nop
  }
  io.freezeOut := io.freezeIn
}

class MEMBarrierContent extends Bundle {
    val instruction = new Instruction
    val PC = UInt(32.W)
    val controlSignals = new ControlSignals
    val ALUResult = UInt(32.W)
    val dataOut = UInt(32.W)
}

class MEMBarrier extends MultiIOModule{
   val io = IO(
    new Bundle {
        val in = Input(new MEMBarrierContent)
        val out = Output(new MEMBarrierContent)
    }
  )

  val regs = Reg(new MEMBarrierContent)
  regs := io.in
  io.out := regs

  val decoder   = Module(new Decoder).io
  decoder.instruction := io.in.instruction

  val memReadReg = RegInit(0.U(1.W))
  memReadReg := decoder.controlSignals.memRead  
  
  when (memReadReg === 1.U) {
    io.out.dataOut := io.in.dataOut
  }.otherwise {
    io.out.dataOut := io.in.ALUResult
  }

}


