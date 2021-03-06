/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.objects.te;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;

import net.dries007.tfc.objects.items.metal.ItemMetalTool;

@ParametersAreNonnullByDefault
public class TEToolRack extends TEBase
{
    /**
     * @return true if this item can be put on a tool rack, false otherwise
     */
    public static boolean isItemEligible(Item item)
    {
        return item instanceof ItemMetalTool || item instanceof ItemTool || item instanceof ItemBow || item instanceof ItemHoe || item instanceof ItemSword;
    }

    public static boolean isItemEligible(@Nullable ItemStack item)
    {
        if (item == null || item.isEmpty())
        {
            return false;
        }
        return isItemEligible(item.getItem());
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);

    public NonNullList<ItemStack> getItems()
    {
        return items;
    }

    public void onBreakBlock()
    {
        items.forEach(i -> InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), i));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        ItemStackHelper.loadAllItems(nbt.getCompoundTag("items"), items);
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setTag("items", ItemStackHelper.saveAllItems(new NBTTagCompound(), items));
        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        super.onDataPacket(net, pkt);
        markBlockUpdate();
    }

    public boolean onRightClick(EntityPlayer player, EnumHand hand, int slot)
    {
        ItemStack slotItem = items.get(slot);
        ItemStack heldItem = player.getHeldItem(hand);
        if (!slotItem.isEmpty())
        {
            ItemHandlerHelper.giveItemToPlayer(player, slotItem.splitStack(1));
            items.set(slot, ItemStack.EMPTY);
        }
        else if (isItemEligible(heldItem))
        {
            items.set(slot, player.isCreative() ? new ItemStack(heldItem.getItem(), 1, heldItem.getMetadata(), heldItem.getTagCompound()) : heldItem.splitStack(1));
        }
        else
        {
            return false;
        }
        markBlockUpdate();
        return true;
    }
}
