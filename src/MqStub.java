import com.ibm.mq.jms.*;

import javax.jms.*;
import java.util.Enumeration;

public class MqStub {
    private static MQQueueConnection mqConn;
    private static MQQueueConnectionFactory mqConnFactory;
    private static MQQueueSession mqSession;
    private static MQQueue mqIn;
    private static MQQueue mqOut;
    private static MQQueueReceiver mqInReciever;
    private static MQQueueSender mqQueueSender;

    public static void main(String[] args) {
        try {


            mqConnFactory = new MQQueueConnectionFactory();
            mqConnFactory.setHostName("localhost");
            mqConnFactory.setPort(1414);
            mqConnFactory.setQueueManager("Marina");
            mqConnFactory.setChannel("System.DEF.SVRCONN");

            mqConn = (MQQueueConnection) mqConnFactory.createQueueConnection();
            mqSession = (MQQueueSession) mqConn.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);

            mqIn = (MQQueue) mqSession.createQueue("MQ.IN");
            mqOut = (MQQueue) mqSession.createQueue("MQ.OUT");

            mqInReciever = (MQQueueReceiver) mqSession.createReceiver(mqIn);
            mqQueueSender = (MQQueueSender) mqSession.createSender(mqOut);

            MessageListener listener = new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    System.out.print("Забрала сообщение из очереди " + mqIn.getBaseQueueName() + ": ");
                    if (msg instanceof TextMessage) {
                        try {
                            TextMessage textMessage = (TextMessage) msg;
                            String messageFromQueue = textMessage.getText();
                            System.out.print(messageFromQueue + "\n");
                            createMessage(messageFromQueue);

                        } catch (JMSException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            mqInReciever.setMessageListener(listener);
            mqConn.start();
            System.out.println("Stub started");


        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createMessage(String msg) throws JMSException {
        TextMessage textMessage = mqSession.createTextMessage(msg);
        mqQueueSender.send(textMessage);
        System.out.println("Отправила сообщение в очередь " + mqOut.getBaseQueueName() + ": " + msg);
        mqSession.commit();
    }
}
