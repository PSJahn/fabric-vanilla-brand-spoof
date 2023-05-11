package de.psjahn.brandspoof.mixin;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import net.fabricmc.fabric.mixin.networking.accessor.CustomPayloadC2SPacketAccessor;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class BrandSpoofMixin {
    @ModifyVariable(at = @At("HEAD"), method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V")
    public Packet<?> modifyPacket(Packet<?> packet)
    {
        return modifiedPacket(packet);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V", ordinal = 0), method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", cancellable = true)
    private void onChannelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci)
    {
        if(cancelPacket(packet)) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", cancellable = true)
    private void onSend(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci)
    {
        if(cancelPacket(packet)) ci.cancel();
    }

    private Packet modifiedPacket(Packet eventPacket)
    {
        if(eventPacket instanceof CustomPayloadC2SPacketAccessor)
        {
            CustomPayloadC2SPacketAccessor packet = (CustomPayloadC2SPacketAccessor) eventPacket;
            if(packet.getChannel().getNamespace().equals("minecraft") && packet.getChannel().getPath().equals("brand"))
                return new CustomPayloadC2SPacket(CustomPayloadC2SPacket.BRAND, new PacketByteBuf(Unpooled.buffer()).writeString("vanilla"));
        }
        return eventPacket;
    }

    private boolean cancelPacket(Packet eventPacket)
    {
        if(eventPacket instanceof CustomPayloadC2SPacketAccessor)
        {
            CustomPayloadC2SPacketAccessor packet = (CustomPayloadC2SPacketAccessor) eventPacket;
            if(packet.getChannel().getNamespace().equals("minecraft") && packet.getChannel().getPath().equals("register"))
                return true;
            if(packet.getChannel().getNamespace().equals("fabric"))
                return true;
        }
        return false;
    }
}
