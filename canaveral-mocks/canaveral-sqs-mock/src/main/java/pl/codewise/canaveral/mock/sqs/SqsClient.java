package pl.codewise.canaveral.mock.sqs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;

class SqsClient {

    private final AmazonSQS client;
    private final int port;

    SqsClient(int port) {
        AWSStaticCredentialsProvider fakeCredentials =
                new AWSStaticCredentialsProvider(new BasicAWSCredentials("foo", "bar"));
        this.client = AmazonSQSClientBuilder.standard()
                .withCredentials(fakeCredentials)
                .withEndpointConfiguration(new EndpointConfiguration("http://localhost:" + port, null))
                .build();
        this.port = port;
    }

    SqsQueue createQueue(String queueName) {
        client.createQueue(new CreateQueueRequest(queueName));
        return new SqsQueue(queueName, this);
    }

    String getQueueUrl(String queueName) {
        return "http://localhost:" + port + "/queue/" + queueName;
    }
}

