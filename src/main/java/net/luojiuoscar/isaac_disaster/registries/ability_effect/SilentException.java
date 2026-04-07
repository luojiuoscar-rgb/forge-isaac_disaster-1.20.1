package net.luojiuoscar.isaac_disaster.registries.ability_effect;

public class SilentException extends RuntimeException {
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // 不生成堆栈，提高错误技环境下的性能
    }
}