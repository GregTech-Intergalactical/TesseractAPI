package tesseract.util;

import net.minecraft.util.math.BlockPos;

public class Pos {
    
    private int x, y, z;

    public Pos() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Pos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Pos(Pos pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    public Pos set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Pos set(Pos pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        return this;
    }

    public Pos add(int x, int y, int z) {
        return set(this.x + x, this.y + y, this.z + z);
    }

    public Pos add(Pos pos) {
        return set(this.x + pos.x, this.y + pos.y, this.z + pos.z);
    }

    public Pos sub(int x, int y, int z) {
        return set(this.x - x, this.y - y, this.z - z);
    }

    public Pos sub(Pos pos) {
        return set(this.x - pos.x, this.y - pos.y, this.z - pos.z);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Pos offset(Dir dir) {
        return new Pos(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset());
    }

    public Pos offset(Dir dir, int n) {
        return n == 0 ? this : new Pos(x + dir.getXOffset() * n, y + dir.getYOffset() * n, z + dir.getZOffset() * n);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pos) {
            Pos o = (Pos) obj;
            return (x == o.x) && (y == o.y) && (z == o.z);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (x + z * 31) * 31 + x;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
