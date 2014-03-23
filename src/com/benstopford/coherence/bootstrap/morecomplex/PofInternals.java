package com.benstopford.coherence.bootstrap.morecomplex;

import com.benstopford.coherence.bootstrap.structures.dataobjects.PofObject;
import com.tangosol.io.ReadBuffer;
import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.PofHelper;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import org.junit.Test;

import java.io.IOException;

public class PofInternals {
    ConfigurablePofContext pofContext = new ConfigurablePofContext("config/my-pof-config.xml");

    @Test
    public void shouldNavigateSingleObject() throws IOException {
        PofObject object = new PofObject("benjamin");

        //get the binary stream
        Binary bob = ExternalizableHelper.toBinary(object, pofContext);
        ReadBuffer.BufferInput stream = bob.getBufferInput();

        //Take off the header - we don't need this
        int headerByte = stream.readPackedInt();
        System.out.printf("Header byte is %s\n", headerByte);

        //The next two are the pof type and its version
        int pofClassId = stream.readPackedInt();
        int pofClassVersion = stream.readPackedInt();
        System.out.printf("Class type-id is %s and version is %s\n", pofClassId, pofClassVersion);

        //next comes the pofId in the stream
        int pofId = stream.readPackedInt();
        System.out.printf("This field has a pofId of %s\n", pofId);

        //next is encoding information
        int encoding = stream.readPackedInt();
        System.out.printf("Field is encoded with type %s\n", encoding);

        //The length of the field is encoded next
        int fieldLength = stream.readPackedInt();
        System.out.printf("Field is of length %s\n", fieldLength);

        //Next is the data
        char[] field = new char[fieldLength];
        for (int i = 0; i < fieldLength; i++) {
            field[i] = PofHelper.readChar(stream);
        }
        System.out.printf("Field is '%s'\n", String.valueOf(field));

        //Closing marker
        System.out.println("Finally there is a closing int: "+stream.readPackedInt());

        //And we're at the end
        System.out.printf("Now at position %s of stream of original length %s\n", stream.getOffset(), bob.length());
    }

}
