package fr.ibrakash.helper.example.sql.blob;

import fr.ibrakash.helper.example.sql.ExampleBadge;
import fr.ibrakash.helper.persistence.entity.PersistedBlobSerializer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExampleBadgeListBlobSerializer implements PersistedBlobSerializer<List<ExampleBadge>> {

    @Override
    public byte[] serialize(List<ExampleBadge> value) {
        if (value == null || value.isEmpty()) return new byte[0];
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeInt(value.size());
            for (ExampleBadge badge : value) {
                data.writeUTF(badge.name());
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExampleBadge> deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return new ArrayList<>();
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            int size = in.readInt();
            List<ExampleBadge> out = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                out.add(ExampleBadge.valueOf(in.readUTF()));
            }
            return out;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExampleBadge> defaultValue() {
        return new ArrayList<>();
    }
}

