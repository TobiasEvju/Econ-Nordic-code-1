package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule
import chisel3.util._

class Forward extends MultiIOModule {
  val io = IO(
    new Bundle {
        //Uint ????? idfk
        val MEMDestReg = Input (UInt(32.W))
        val MEMResult = Input(UInt(32.W))
        val MEMLoad = Input(Bool())

        val WBDestReg = Input (UInt(32.W))
        val WBResult = Input(UInt(32.W))

        val WBLateDestReg = Input (UInt(32.W))
        val WBLateResult = Input(UInt(32.W))

        val WBLateLateDestReg = Input (UInt(32.W))
        val WBLateLateResult = Input(UInt(32.W))

        val EXR1 = Input (UInt(32.W))
        val EXR2 = Input(UInt(32.W))
        val EXR1Value = Input(UInt(32.W))
        val EXR2Value = Input(UInt(32.W))

        val R1Output = Output(UInt(32.W))
        val R2Output = Output(UInt(32.W))
        val ForwardLoadMem = Output(Bool())

    })

    io.ForwardLoadMem := false.B
    when (io.EXR1 === io.MEMDestReg || io.EXR2 === io.MEMDestReg) {
        when (io.EXR1 === io.MEMDestReg) {
            io.R1Output := io.MEMResult
            when(io.MEMLoad) {io.ForwardLoadMem := true.B}
        }.elsewhen(io.EXR1 === io.WBDestReg){
            io.R1Output := io.WBResult
        }.elsewhen(io.EXR1 === io.WBLateDestReg) {
            io.R1Output := io.WBLateResult
        }.elsewhen(io.EXR1 === io.WBLateLateDestReg) {
            io.R1Output := io.WBLateLateResult
        }.otherwise {
            io.R1Output := io.EXR1Value
        }

        when (io.EXR2 === io.MEMDestReg){
            io.R2Output := io.MEMResult
            when(io.MEMLoad) {io.ForwardLoadMem := true.B}
        }.elsewhen(io.EXR2 === io.WBDestReg){
            io.R2Output := io.WBResult
        }.elsewhen(io.EXR2 === io.WBLateDestReg) {
            io.R2Output := io.WBLateResult
        }.elsewhen(io.EXR2 === io.WBLateLateDestReg) {
            io.R2Output := io.WBLateLateResult
        }.otherwise {
            io.R2Output := io.EXR2Value
        }
    }
    
    .elsewhen(io.EXR1 === io.WBDestReg || io.EXR2 === io.WBDestReg) {
        when (io.EXR1 === io.WBDestReg) {
            io.R1Output := io.WBResult
        }.elsewhen(io.EXR1 === io.WBLateDestReg) {
            io.R1Output := io.WBLateResult
        }.elsewhen(io.EXR1 === io.WBLateLateDestReg) {
            io.R1Output := io.WBLateLateResult
        }.otherwise {
            io.R1Output := io.EXR1Value
        }
        when (io.EXR2 === io.WBDestReg){
            io.R2Output := io.WBResult
        }.elsewhen(io.EXR2 === io.WBLateDestReg) {
            io.R2Output := io.WBLateResult
        }.elsewhen(io.EXR2 === io.WBLateLateDestReg) {
            io.R2Output := io.WBLateLateResult
        }.otherwise {
            io.R2Output := io.EXR2Value
        }
    }
    
    .elsewhen(io.EXR1 === io.WBLateDestReg || io.EXR2 === io.WBLateDestReg) {
        when (io.EXR1 === io.WBLateDestReg) {
            io.R1Output := io.WBLateResult
        }.elsewhen(io.EXR1 === io.WBLateLateDestReg) {
            io.R1Output := io.WBLateLateResult
        }.otherwise {
            io.R1Output := io.EXR1Value
        }
        when (io.EXR2 === io.WBLateDestReg){
            io.R2Output := io.WBLateResult
        }.elsewhen(io.EXR2 === io.WBLateLateDestReg) {
            io.R2Output := io.WBLateLateResult
        }.otherwise {
            io.R2Output := io.EXR2Value
        }
    }
    
    .elsewhen(io.EXR1 === io.WBLateDestReg || io.EXR2 === io.WBLateDestReg) {
        when(io.EXR1 === io.WBLateLateDestReg) {
            io.R1Output := io.WBLateLateResult
        }.otherwise {
            io.R1Output := io.EXR1Value
        }
        when(io.EXR2 === io.WBLateLateDestReg) {
            io.R2Output := io.WBLateLateResult
        }.otherwise {
            io.R2Output := io.EXR2Value
        }
    }.otherwise{
        io.R1Output := io.EXR1Value
        io.R2Output := io.EXR2Value
        io.ForwardLoadMem := false.B
    }
    
}
