package org.pipeman.pipo.mixin;

//@Mixin(Entity.class)
//public abstract class EntityMixin {
//    @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPos(DDD)V"), index = 0)
//    public double aVoid1(double value) {
//        return Double.isFinite(value) ? value : 0;
//    }
//
//    @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPos(DDD)V"), index = 1)
//    public double aVoid2(double value) {
//        return Double.isFinite(value) ? value : 0;
//    }
//
//    @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPos(DDD)V"), index = 2)
//    public double aVoid3(double value) {
//        return Double.isFinite(value) ? value : 0;
//    }
//}
