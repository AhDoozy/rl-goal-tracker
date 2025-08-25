package com.toofifty.goaltracker.models.task;

import com.google.gson.annotations.SerializedName;
import com.toofifty.goaltracker.models.enums.Status;
import com.toofifty.goaltracker.models.enums.TaskType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.awt.image.BufferedImage;

@Setter
@Getter
@SuperBuilder
/**
 * Task representing acquisition of an item.
 * Tracks item ID, name, desired quantity, acquired count, and cached icon.
 */
public final class ItemTask extends Task
{
    private transient BufferedImage cachedIcon;
    @Builder.Default
    @SerializedName("quantity")
    private int quantity = 1;
    @Builder.Default
    @SerializedName("acquired")
    private int acquired = 0;
    @SerializedName("item_id")
    private int itemId;
    @SerializedName("item_name")
    private String itemName;

    /**
     * Recompute acquired count and task status from a current item count.
     * @param count how many of this item are currently held (inventory/equipment/bank, as defined by the caller)
     * @return true if either acquired or status changed; false otherwise
     */
    public boolean recomputeFromCount(final int count)
    {
        final int oldAcquired = this.acquired;
        final Status oldStatus = getStatus();

        this.acquired = Math.max(0, Math.min(count, Math.max(1, quantity)));

        if (this.acquired >= this.quantity)
        {
            setStatus(Status.COMPLETED);
        }
        else if (this.acquired > 0)
        {
            setStatus(Status.IN_PROGRESS);
        }
        else
        {
            setStatus(Status.NOT_STARTED);
        }

        return oldAcquired != this.acquired || oldStatus != getStatus();
    }

    @Override
    public String toString()
    {
        if (quantity == 1) {
            return itemName;
        }

        if (acquired > 0 && acquired < quantity) {
            return String.format("%d/%d x %s", acquired, quantity, itemName);
        }

        return String.format("%d x %s", quantity, itemName);
    }

    @Override
    public String getDisplayName()
    {
        return itemName;
    }

    @Override
    public TaskType getType()
    {
        return TaskType.ITEM;
    }
}
