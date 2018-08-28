package pl.codewise.canaveral.mock.s3;


import org.joda.time.DateTime;


public interface S3MockObject {

    static S3MockObject from(String key, byte[] content, DateTime lastModified) {
        return new S3MockObject() {

            @Override
            public String key() {
                return key;
            }

            @Override
            public byte[] content() {
                return content;
            }

            @Override
            public DateTime lastModified() {
                return lastModified;
            }
        };
    }

    String key();

    byte[] content();

    DateTime lastModified();
}