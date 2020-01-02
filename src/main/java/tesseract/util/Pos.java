package tesseract.util;

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
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Pos add(Pos pos) {
        this.x += pos.x;
        this.y += pos.y;
        this.z += pos.z;
        return this;
    }

    public Pos sub(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Pos sub(Pos pos) {
        this.x -= pos.x;
        this.y -= pos.y;
        this.z -= pos.z;
        return this;
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
        return offset(dir, 1);
    }

    public Pos offset(Dir dir, int count) {
        if (count == 0) return this;
        this.x += dir.getOffset() * count;
        this.y += dir.getOffset() * count;
        this.z += dir.getOffset() * count;
        return this;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
