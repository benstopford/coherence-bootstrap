package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.dataobjects.LengthyPofObject;
import com.benstopford.coherence.bootstrap.structures.dataobjects.PofObject;
import com.tangosol.io.ReadBuffer;
import com.tangosol.io.pof.PofHelper;
import com.tangosol.io.pof.PortableObjectSerializer;
import com.tangosol.io.pof.SimplePofContext;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PofInternals {

    @Test
    public void navigateSingleObject() throws IOException {
        SimplePofContext context = new SimplePofContext();
        context.registerUserType(1042, PofObject.class, new PortableObjectSerializer(1042));

        PofObject object = new PofObject("TheData");

        //get the binary stream
        Binary binary = ExternalizableHelper.toBinary(object, context);
        ReadBuffer.BufferInput stream = binary.getBufferInput();

        //****OBJECT HEADER****

        //Take off the header - we don't need this
        int headerByte = stream.readPackedInt();
        assertThat(headerByte, is(21));

        //The next two bytes are the classId
        int pofClassId = stream.readPackedInt();
        assertThat(pofClassId, is(1042));

        //Then the version (for evolvability purposes)
        int pofClassVersion = stream.readPackedInt();
        assertThat(pofClassVersion, is(0));

        //****FIELD HEADER****

        //The next byte is the pof id of the first field, 1
        int pofId = stream.readPackedInt();
        assertThat(pofId, is(1));

        //The next byte the field type, in this case it's String (-15)
        int datatype = stream.readPackedInt();
        assertThat(datatype, is(-15));

        //Then a byte for the field length. It's the string 'benjamin' so we expect 8 bytes
        int fieldLength = stream.readPackedInt();
        assertThat(fieldLength, is("TheData".length()));

        ////****FIELD VALUE****

        char[] field = new char[fieldLength];
        for (int i = 0; i < fieldLength; i++) {
            field[i] = PofHelper.readChar(stream);
        }
        assertThat(String.valueOf(field), is("TheData"));

        //And we're at the end (There is a trailing byte if you use PortableObject serialisation - I'm not sure why)
        assertThat(stream.getOffset() + 1, is(binary.length()));

        //reset the stream
        stream = binary.getBufferInput();

        System.out.printf("Header btye: %s\n" +
                        "ClassType is: %s\n" +
                        "ClassVersion is: %s\n" +
                        "FieldPofId is: %s\n" +
                        "Field data type is: %s\n" +
                        "Field length is: %s\n",
                stream.readPackedInt(),
                stream.readPackedInt(),
                stream.readPackedInt(),
                stream.readPackedInt(),
                stream.readPackedInt(),
                stream.readPackedInt()
        );
        System.out.printf("Field Value is: %s\n",
                binary.toBinary(6, "TheData".length() + 1).getBufferInput().readSafeUTF()
        );

    }


    @Test
    public void navigateObjectWithFields() throws IOException {
        SimplePofContext context = new SimplePofContext();
        context.registerUserType(1042, LengthyPofObject.class, LengthyPofObject.serializer);

        //this time create an tiered object
        LengthyPofObject object = new LengthyPofObject(50, 51, "fifty two", 53, "fifty four");

        //get the binary stream
        Binary bob = ExternalizableHelper.toBinary(object, context);
        ReadBuffer.BufferInput stream = bob.getBufferInput();

        System.out.println("Header byte is: " + stream.readPackedInt());
        System.out.println("Class type is: " + stream.readPackedInt());
        System.out.println("Class version is: " + stream.readPackedInt());
        System.out.println("Field Pof Id is: " + stream.readPackedInt());
        System.out.println("Field data type is: " + stream.readPackedInt());
        System.out.println("Number of fields: " + stream.readPackedInt());

        while (stream.available() > 1) {
            System.out.printf("{pofid:" + stream.readPackedInt()+", ");
            int type = stream.readPackedInt();
            System.out.printf(",typeId:" + type);
            if (type == -2)
                System.out.printf("(int), Value:" + stream.readPackedInt() + "}\n");
            if (type == -15) {
                int stringLen = stream.readPackedInt();
                char[] field = new char[stringLen];
                for (int i = 0; i < stringLen; i++) {
                    field[i] = PofHelper.readChar(stream);
                }
                System.out.printf("(str), Value:'" + String.valueOf(field) + "'}\n");
            }
        }
    }


    @Test
    public void navigateNestedObject() throws IOException {
        SimplePofContext context = new SimplePofContext();
        context.registerUserType(1042, PofObject.class, new PortableObjectSerializer(1042));

        //this time create an tiered object
        PofObject object = new PofObject(new PofObject("wrapped-value"));

        //get the binary stream
        Binary bob = ExternalizableHelper.toBinary(object, context);
        ReadBuffer.BufferInput stream = bob.getBufferInput();

        System.out.println("Header byte is: " + stream.readPackedInt());
        System.out.println("Class type is: " + stream.readPackedInt());
        System.out.println("Class version is: " + stream.readPackedInt());
        System.out.println("Field Pof Id is: " + stream.readPackedInt());
        System.out.println("Field data type is: " + stream.readPackedInt()); //1000 as it's the nested PofObject
        System.out.println("Version (of nested object) is: " + stream.readPackedInt());
        System.out.println("Field Pof Id (in nested object) is: " + stream.readPackedInt());
        System.out.println("Field Datatype (in nested object) is: " + stream.readPackedInt());

        int length = stream.readPackedInt();
        System.out.println("Length (of nested object) is: " + length);

        char[] field = new char[length];
        for (int i = 0; i < length; i++) {
            field[i] = PofHelper.readChar(stream);
        }
        System.out.printf("Value is '%s'\n", String.valueOf(field));

        //And we're at the end (There are two trailing bytes if you use PortableObject serialisation - one per object - I'm not sure why)
        assertThat(stream.getOffset() + 2, is(bob.length()));
    }
}
