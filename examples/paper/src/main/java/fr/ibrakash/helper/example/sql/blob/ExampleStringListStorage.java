package fr.ibrakash.helper.example.sql.blob;

import fr.ibrakash.helper.binary.BinaryListStorage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExampleStringListStorage extends BinaryListStorage<String> {

    public ExampleStringListStorage() {
        super(new ArrayList<>());
    }

    public ExampleStringListStorage(List<String> from) {
        super(new ArrayList<>(from));
    }

    @Override
    protected byte[] serialize(List<String> value) {
        if (value == null || value.isEmpty()) return EMPTY_ARRAY;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeInt(value.size());
            for (String entry : value) {
                data.writeUTF(entry == null ? "" : entry);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void deserialize(byte[] array) {
        this.value.clear();
        if (array == null || array.length == 0) return;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(array))) {
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                this.value.add(in.readUTF());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

