package pl.codewise.canaveral.mock.sqs;

class SqsQueue {

    private final String queueName;
    private final SqsClient sqsClient;

    SqsQueue(String queueName, SqsClient sqsClient) {
        this.queueName = queueName;
        this.sqsClient = sqsClient;
    }

    String getQueueUrl() {
        return sqsClient.getQueueUrl(queueName);
    }
}

