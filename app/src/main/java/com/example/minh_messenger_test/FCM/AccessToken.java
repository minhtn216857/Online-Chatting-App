package com.example.minh_messenger_test.FCM;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AccessToken {
    private static final String firebaseMessagingScope
            = "https://www.googleapis.com/auth/firebase.messaging";

    public String getAccessToken() {
        try {
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"messengerminh\",\n" +
                    "  \"private_key_id\": \"435d30993486630de22a7dc68bc100ee2337e4ae\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDJ0cor8H2nShh9\\nCzrXw63epAfwcW2Lymq0qfgXSaxS7dKwiFryPheIlmkhL9ezIBH3LXdAD0LmpKv5\\nnwPuowM79BNHnpTbDzoCawI3um94V84wm2Irp13BESp4Z9f+SCqs/JWts9SQSMmt\\ntAgb53XO9SKdiNOAJV5q+SVjl1ll0BPB+lz8TlcZp5SVk2ifdFWB9u9WoV+C3xgK\\nleQylwXgVkYUcEwfUSyKFn47AQnyXdypQAkVDjPV1uGNidi4+RrR9m3il8/zlgGe\\nc3ksfEecf4XO2OV9/5d1x7NRI9Esx+Kz7R7jEDUR6a/i0arafKSht1fVLdGejJPQ\\ngb28g+pvAgMBAAECggEACkVDXnYS0Zsgi5MxOj6/5UHVZ5lAzmmNgZvZahylM46H\\nzNUQ78f/fEN589GSpA/V6jglm/HK4UrwYjfYmYyGgxwPiR2wYe+F77k7Yn5POEFx\\nAaHpXo2vZJO3j2tqygrTz9GGL9H5giLkzz1ctkgoA3wPzWg4/zSnopZYSUJarybO\\n9fufTg20e7Dq95TsrCr6EnP5Q53T1yD/qG4J2AHJXPi6PHwGyFlcDM7iekhcyAVl\\n3Vjv035Lt1iVAPlj5cIPfWRoMXTxEkcEg/s8s04HF73eb3WqztnehB+DbPLVzsii\\ndrUw7fY/6iQFMSbgwaPGGgxEHxZnknVb27kuEAmdwQKBgQD3IMtM5+/q4XqKCZJc\\n9Ey55ffh16bYlNAW6tvrNit/GZx2ipL495cpdiCE7NlyASV4pQmoC/x5sSyr5S2/\\nwVhg8w5bqGz0GuoAcxdV5tGil846BgdlU2lJdAsGD4MC5yt21+Fk+eR1XxzP9H3v\\nk0Is+w4rVbZNZWtyZBvOvGW4rwKBgQDREJdkltSqT8lb7EeKdyCHGEbEJaNpCT5D\\n4hMrc2W6enkJaRGdYHCQBJn7uR25qSGGkHBERPgmlAsJzI8mu+NOpMvHhtnTcz4Q\\nn5PPxO0f7Ny6M21S8RuHLduRa/p6QFuHepcErIgCJAXknN5wnfzqKWb7mgXnGQL3\\nTXzJFAXaQQKBgQD0GpmWMrgCe4yOExxW4x1yttyeYWS2JHBTYtOuR4i5eDPbx8+b\\nSv0ugDdo9YdoHAfGXsbmDkjqb37foHYLJlFFGnDBSuGsUXV40jZnrKLAB1E2cUQW\\n60hhvNUyvFm9oViGgIFsO4ZWBYgxRaCr5/qC8c2yMmzrkFWM3aO2FtQnkwKBgDGV\\n9dJt09LxSZj72ZwF1Tk1kNNE+hUXlCUVoeZUYHRyR/LrcLoa/Zeq/7qslVhLvR/l\\ncjs/AHxl8JhKZEmTlr44Z/zTBycX1kirEKve0T8ZNuETAYMCnLzbK5C6Q5ZrXUos\\n0VTSdw/K63rlmfyr/Cit2TcjuPKvA/0T69zlWt4BAoGBAKx6pEAN10ElSiUbz2Zl\\nFJmfnj0QEPCqQrXQDQWae62VM5N0Y7gD/LESuVAUmr8i0kfeZLN5unUIYJDFGXOw\\ntgT4MRi1EzItzEA3f4RUJxRW5VYOWV0i9i3TmePdmU2brXG+qKngMQliCdYQBuCY\\nUg4auUqdXXUQFQ56swg0r9T/\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-xzzrm@messengerminh.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"111505576486204255463\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-xzzrm%40messengerminh.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";

            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Lists.newArrayList(firebaseMessagingScope));

            googleCredentials.refresh();

            return googleCredentials.getAccessToken().getTokenValue();

        } catch (Exception e) {
            Log.e("ERROR", "" + e.getMessage());
            return null;
        }
    }
}
