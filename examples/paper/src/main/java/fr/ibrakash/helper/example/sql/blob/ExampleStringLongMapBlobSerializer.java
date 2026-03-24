package fr.ibrakash.helper.example.sql.blob;

import fr.ibrakash.helper.persistence.entity.PersistedBlobSerializer;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExampleStringLongMapBlobSerializer implements PersistedBlobSerializer<Map<String, Long>> {

    @Override
    public byte[] serialize(Map<String, Long> value) {
        if (value == null || value.isEmpty()) return new byte[0];
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeInt(value.size());
            for (Map.Entry<String, Long> entry : value.entrySet()) {
                data.writeUTF(entry.getKey());
                data.writeLong(entry.getValue() == null ? 0L : entry.getValue());
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Long> deserialize(byte[] bytes) {
        Map<String, Long> map = new LinkedHashMap<>();
        if (bytes == null || bytes.length == 0) return map;
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                map.put(in.readUTF(), in.readLong());
            }
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Long> defaultValue() {
        return new LinkedHashMap<>();
    }
}

