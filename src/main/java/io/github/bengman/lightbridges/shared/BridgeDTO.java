package io.github.bengman.lightbridges.shared;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public class BridgeDTO {

    private List<BridgeSegmentDTO> segments;

    public BridgeDTO() {
        this.segments = new ArrayList<BridgeSegmentDTO>();
    }

    public BridgeDTO(
            List<BridgeSegmentDTO> segments) {

        this.segments = segments;
    }

    public List<BridgeSegmentDTO> getSegments() {
        return segments;
    }

    public CompoundNBT toNBT() {

        CompoundNBT tag = new CompoundNBT();

        ListNBT segmentList = new ListNBT();

        for (BridgeSegmentDTO segment : segments) {
            segmentList.add(segment.toNBT());
        }

        tag.put("segments", segmentList);

        return tag;
    }

    public static BridgeDTO fromNBT(
            CompoundNBT tag) {

        ListNBT segmentList = tag.getList("segments", 10);

        List<BridgeSegmentDTO> segments = new ArrayList<>();

        for (int i = 0; i < segmentList.size(); i++) {

            CompoundNBT segmentTag = segmentList.getCompound(i);

            segments.add(
                    BridgeSegmentDTO.fromNBT(segmentTag));
        }

        return new BridgeDTO(segments);
    }
}