package fit.iuh.student.communicationservice.consumers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatBotConsumer {

    @RabbitListener(queues = "CHAT_BOT_QUEUE")
    public void handleChatBotEvent(){
        try{
            log.info("Chat bot event received");

        }catch (Exception e){
            throw e;
        }
    }
}
