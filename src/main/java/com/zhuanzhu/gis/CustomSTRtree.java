package com.zhuanzhu.gis;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.AbstractNode;
import org.locationtech.jts.index.strtree.Boundable;
import org.locationtech.jts.index.strtree.ItemBoundable;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.util.Assert;

import java.util.List;

public class CustomSTRtree extends STRtree {
    public CustomSTRtree() {
        super();
    }

    public CustomSTRtree(int nodeCapacity) {
        super(nodeCapacity);
    }

    public Object findFirstChild(Envelope envelope){
        if (this.isEmpty() ) {
            return null;
        }
        if (this.getIntersectsOp().intersects(this.root.getBounds(), envelope)) {
            return this.findFirstChildInternal(envelope, this.root);
        }
        return  null;
    }
    private Object findFirstChildInternal(Envelope searchEnv, AbstractNode node) {
        List childBoundables = node.getChildBoundables();

        for(int i = 0; i < childBoundables.size(); ++i) {
            Boundable childBoundable = (Boundable)childBoundables.get(i);
            if (this.getIntersectsOp().intersects(childBoundable.getBounds(), searchEnv)) {
                if (childBoundable instanceof AbstractNode) {
                    return findFirstChild(searchEnv);
                } else if (childBoundable instanceof ItemBoundable) {
                    return ((ItemBoundable)childBoundable).getItem();
                } else {
                    Assert.shouldNeverReachHere();
                }
            }
        }
        return null;
    }
}
