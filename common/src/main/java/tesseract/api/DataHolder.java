package tesseract.api;

public abstract class DataHolder<T, U> {
    final T immutableData;
    U data;
    public DataHolder(T immutableData, U data){
        this.immutableData = immutableData;
        this.data = data;
    }


    public T getImmutableData() {
        return immutableData;
    }

    public U getData(){
        return data;
    }

    public void setData(U data){
        this.data = data;
    }
}
