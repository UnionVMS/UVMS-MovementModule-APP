package eu.europa.fisheries.uvms.component.service.arquillian;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ejb.EJB;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.MovementModuleMethod;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JMSUtils;
import eu.europa.ec.fisheries.uvms.movement.message.constants.MessageConstants;
import eu.europa.ec.fisheries.uvms.movement.message.consumer.bean.CreateMovementBean;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.movement.model.mapper.MovementModuleRequestMapper;

/**
 * Created by thofan on 2017-04-19.
 */

/**  OBS These tests are postponed to a later stage, since mix of interfacelayer and businesslayer makes it not testabale at this time
 *
 *
 *
 *
 */

@RunWith(Arquillian.class)
@Ignore
public class MessageConsumerBeanIntTest extends TransactionalTests {


    @EJB
    CreateMovementBean createMovementBean;


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
     * make this a textMessage so we dont need the Activie mq at all for the test
     *
     * @throws Exception
     */

    @Test
    @OperateOnDeployment("movementmessage")
    public void messageConsumerBeanTestCreateMovement() throws Exception {

        eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType movementBaseType = new eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType();
        CreateMovementRequest createMovementRequest = new CreateMovementRequest();
        createMovementRequest.setMethod(MovementModuleMethod.CREATE);
        createMovementRequest.setMovement(movementBaseType);

        Queue test = JMSUtils.lookupQueue(MessageConstants.AUDIT_MODULE_QUEUE);

        String nafMovement= movements[0];
        SetReportMovementType mappedNafMovement = NafMessageResponseMapper.mapToMovementType(nafMovement, "NAF");



        String met = JAXBMarshaller.marshallJaxBObjectToString(mappedNafMovement.getMovement());

        TextMessage textMessage = mock(TextMessage.class);
        when(textMessage.getText()).thenReturn(met);


        //eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType movementBaseType = JAXBMarshaller.unmarshallTextMessage(textMessage, eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType.class);

        String requestStr = MovementModuleRequestMapper.mapToCreateMovementRequest(movementBaseType,"TEST");
        //String nafMovementStr = ExchangeModuleRequestMapper.createSetMovementReportRequest(mappedNafMovement, "TEST");


//        TextMessage activeMQTextMessage = new ActiveMQTextMessage();
        when(textMessage.getText()).thenReturn(requestStr);
        when(textMessage.getJMSReplyTo()).thenReturn(test);
        when(textMessage.getJMSMessageID()).thenReturn("TESTER1234");

        createMovementBean.createMovement(textMessage);

    }


    /******************************************************************************************************************************
     *
     ******************************************************************************************************************************/

}