package eu.europa.fisheries.uvms.component.service.arquillian;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.movement.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.movement.message.producer.bean.MessageProducerBean;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.*;

/**
 * Created by thofan on 2017-04-19.
 */

@RunWith(Arquillian.class)
public class MessageConsumerBeanIntTest extends TransactionalTests {


    String movements[] = {

            "//SR//AD/SWE//TM/POS//RC/DCGU//IR/DEU000840300//XR/ACC4//LT/53.719//LG/7.449//DA/20170412//TI/0444//SP/24//CO/245//NA/Freya//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DJCH//IR/DEU500400102//XR/SD10//LT/54.128//LG/8.863//DA/20170412//TI/0444//SP/0//CO/316//NA/Christine//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DJIG//IR/DEU500550108//XR/ACC1//LT/53.718//LG/7.471//DA/20170412//TI/0444//SP/10//CO/302//NA/Gerda-Bianka//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DKOC//IR/DNK000026630//XR/SH7//LT/56.004//LG/8.124//DA/20170412//TI/0444//SP/0//CO/43//NA/Gitte//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DCDB//IR/DEU001770300//XR/NOR225//LT/53.625//LG/7.161//DA/20170412//TI/0444//SP/0//CO/48//NA/Nordmeer//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DCFE2//IR/DEU002180300//XR/GRE37//LT/54.529//LG/7.963//DA/20170412//TI/0444//SP/16//CO/97//NA/Lambert//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DCOH//IR/DEU001900300//XR/GRE2//LT/53.503//LG/7.099//DA/20170412//TI/0444//SP/0//CO/247//NA/Erna//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DIUZ//IR/DEU400370107//XR/WRE2//LT/53.649//LG/8.495//DA/20170412//TI/0444//SP/0//CO/38//NA/Polli//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DITZ//IR/DEU400410107//XR/SD22//LT/54.129//LG/8.864//DA/20170412//TI/0444//SP/0//CO/15//NA/Kormoran//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DIUO//IR/DEU400460107//XR/ACC14//LT/53.725//LG/7.563//DA/20170412//TI/0444//SP/62//CO/86//NA/Gerda Katharina//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DJDF//IR/DEU500190105//XR/PEL15//LT/54.518//LG/8.547//DA/20170412//TI/0444//SP/26//CO/178//NA/Wencke//FS/DEU//FR/DEU/ER//",
            "//SR//AD/SWE//TM/POS//RC/DCLC//IR/DEU001420300//XR/ST10//LT/54.267//LG/8.849//DA/20170412//TI/0444//SP/0//CO/7//NA/Jule Marie//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DFNZ//IR/DEU000160300//XR/CUX9//LT/53.864//LG/8.704//DA/20170412//TI/0444//SP/0//CO/36//NA/Ramona//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DCPU//IR/DEU001310300//XR/GRE22//LT/53.501//LG/7.095//DA/20170412//TI/0444//SP/0//CO/273//NA/Frieda-Luise//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DJIV//IR/DEU500200105//XR/GRE13//LT/53.502//LG/7.097//DA/20170412//TI/0444//SP/0//CO/21//NA/Jan Looden//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DIWD//IR/DEU400660101//XR/SH16//LT/53.861//LG/8.713//DA/20170412//TI/0444//SP/0//CO/22//NA/Marie Louise//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DJIS//IR/DEU500630124//XR/HOO1//LT/54.575//LG/8.542//DA/20170412//TI/0444//SP/0//CO/244//NA/De Liekedeelers//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DCEQ//IR/DEU001230300//XR/GRE14//LT/53.503//LG/7.099//DA/20170412//TI/0444//SP/0//CO/23//NA/Wangerland//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DLYQ//IR/DEU500410102//XR/HUS28//LT/54.473//LG/9.045//DA/20170412//TI/0444//SP/0//CO/50//NA/Zukunft//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DF7893//IR/DEU000660300//XR/DOR5//LT/53.738//LG/8.517//DA/20170412//TI/0444//SP/0//CO/46//NA/Nixe II//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DCET//IR/DEU000960300//XR/NEU232//LT/53.762//LG/7.649//DA/20170412//TI/0444//SP/70//CO/17//NA/Moewe//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DMXQ//IR/DEU001990300//XR/GRE4//LT/53.503//LG/7.099//DA/20170412//TI/0444//SP/0//CO/51//NA/Magellan//FS/DEU//FR/DEU//ER//",
            "//SR//AD/SWE//TM/POS//RC/DESC//IR/DEU000390300//XR/FED8//LT/53.597//LG/8.355//DA/20170412//TI/0444//SP/0//CO/22//NA/Harmonie//FS/DEU//FR/DEU//ER//",};


    /**
     * messages is how movements looks when coming into the system
     * first mapper creates SetReportMovementType
     * second mapper creates a string that is the format that is posted to ActiveMQ
     * <p>
     * the ideal thing would be to make this a textMessage so we dont need the Activie mq at all for the test
     * otherwise post it to the queue
     *
     * @throws Exception
     */

    @Test
    @OperateOnDeployment("movementservice")
    public void messageConsumerBeanTestCreateMovement() throws Exception {

        System.setProperty(MessageProducerBean.MESSAGE_PRODUCER_METHODS_FAIL, "false");

        String a_NAF_Message = movements[0];
        SetReportMovementType mapped = NafMessageResponseMapper.mapToMovementType(a_NAF_Message, "NAF");
        String movementReportRequest = ExchangeModuleRequestMapper.createSetMovementReportRequest(mapped, "TEST");

        sendToQueue(movementReportRequest);


    }


    /******************************************************************************************************************************
     *
     ******************************************************************************************************************************/


    // close but no cigar
    public void sendToQueue(String msg) {


        /*
        Connection connection = null;
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("customerQueue");
            MessageProducer producer = session.createProducer(queue);
            String payload = "Hi, I am text message";
            Message msg = session.createTextMessage(payload);
            System.out.println("Sending text '" + payload + "'");
            producer.send(msg);
            session.close();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        */


    }
}