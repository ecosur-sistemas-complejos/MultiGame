package mx.ecosur.multigame.util {
	import flash.events.EventDispatcher;
	
	import mx.controls.Alert;
	import mx.events.DynamicEvent;
	import mx.messaging.Consumer;
	import mx.messaging.events.MessageEvent;
	import mx.messaging.events.MessageFaultEvent;
	import mx.messaging.messages.IMessage;
	
	/**
	 * Wraps a message Consumer to create a message queue. Messages are
	 * received asyncronously and filtered by their gameId. The filtered messages are
	 * then dispatched in order of ascending id via the MessageReceiver.PROCESS_MESSAGE 
	 * event. A message will not be dispatched until all the messages with inferior ids
	 * have been received and dispatched.
	 */
	public class MessageReceiver extends EventDispatcher{
		
		private var _messages:Array; //internal array of messages that have not been processed
		private var _consumer:Consumer; //flex message consumer component
		private var _lastMsgId:int; //id of the last message processed. Used as a message counter to ensure that messages are processed in the correct order.
		
		public static const PROCESS_MESSAGE:String = "processMessage";
		[Event (name = PROCESS_MESSAGE, type="mx.events.DynamicEvent")]
		
		/**
		 * Constructor. Requires the id of the game being played so that only messages
		 * for this game get handled and the name of the jms consumer destination
		 * as defined in messaging-config.xml
		 *  
		 * @param destination the jms destination as defined in messaging-config.xml
		 * @param gameId the id of the game being played
		 * 
		 */
		public function MessageReceiver(destination:String, gameId:int) {
			
			//initialize empty array of messages
			_messages = new Array();
			
			//TODO:Filter messages by game id
			//initialize consumer
			_consumer = new Consumer();
			_consumer.destination = destination;
			_consumer.selector = "GAME_ID = " + gameId;
			_consumer.addEventListener(MessageEvent.MESSAGE, handleMessage);
			_consumer.addEventListener(MessageFaultEvent.FAULT, handleFault);
			_consumer.subscribe();
		}
		
		/*
		 * Adds the message to the queue and tries to process the queue
		 */
		private function handleMessage(event:MessageEvent):void {
			_messages.push(IMessage(event.message));
			processQueue();
		}
		
		private function handleFault(event:MessageFaultEvent):void {
			Alert.show(event.faultString, "Error receiving message");
		}
		
		/*
		 * Reorders messages in the queue and dispatches the messages as events
		 * starting with the lowest id until either no messages are left in the
		 * queue or a gap is found in the id's of the messages in the queue
		 */
		private function processQueue():void{
			_messages.sort(compare);
			var msg:IMessage;
			while(_messages.length > 0){
				msg = IMessage(_messages[0]);
				if (_lastMsgId == 0 || msg.headers.MESSAGE_ID == _lastMsgId + 1){
					_lastMsgId = msg.headers.MESSAGE_ID;
					//Alert.show("processing message with id " + _lastMsgId + "\n Game event = " + msg.headers.GAME_EVENT, "Processing message");
					var evt:DynamicEvent = new DynamicEvent(PROCESS_MESSAGE);
					evt.message = msg;
					dispatchEvent(evt);
					_messages.splice(0, 1);
				}else{
					//Alert.show("broken sequence waiting for message with id " + Number(_lastMsgId + 1) + "\n but receivd " + msg.headers.MESSAGE_ID, "Broken sequence");
					break;
				}
			}
		}
		
		private function compare(messageA:IMessage, messageB:IMessage):int{
			var messageAId:int = messageA.headers.MESSAGE_ID;
			var messageBId:int = messageB.headers.MESSAGE_ID;
			if (messageAId < messageBId){
				return -1;
			} else if (messageBId < messageAId){
				return 1; 
			}
			return 0;
		}

	}
}
