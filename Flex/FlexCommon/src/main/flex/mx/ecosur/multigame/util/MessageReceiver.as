    /*
    * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
    *
    * Licensed under the Academic Free License v. 3.2.
    * http://www.opensource.org/licenses/afl-3.0.php
    */

    /**
    * @author max@alwayssunny.com
    */

    package mx.ecosur.multigame.util {
    import flash.events.EventDispatcher;
    import flash.events.TimerEvent;
    import flash.utils.Timer;

    import mx.controls.Alert;
    import mx.ecosur.multigame.enum.GameEvent;
    import mx.events.DynamicEvent;
    import mx.messaging.ChannelSet;
    import mx.messaging.Consumer;
    import mx.messaging.channels.AMFChannel;
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
        private var _lastMoveCompleted:Date; //the time the last move complete message was received.
        private var _processMessagesTimer:Timer; //delays the dispatching of messages to force waits inbetween moves

        private static var MINIMUM_MOVE_TIME:Number = 5000; //minimum number of miliseconds to wait between dispatching move complete messages

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
        public function MessageReceiver(destination:String, gameId:int, channelSet:ChannelSet= null) {

            //initialize empty array of messages
            _messages = new Array();

            //initialize consumer
            _consumer = new Consumer();
            _consumer.destination = destination;
            _consumer.selector = "GAME_ID = " + gameId;
            _consumer.addEventListener(MessageEvent.MESSAGE, handleMessage);
            _consumer.addEventListener(MessageFaultEvent.FAULT, handleFault);
           
            if (channelSet)
                _consumer.channelSet = channelSet;
            _consumer.subscribe();

            //initialize the time
            _processMessagesTimer = new Timer(1000, 1);
            _processMessagesTimer.addEventListener(TimerEvent.TIMER, handleTimer);
            
            
        }

        public function destroy():void{
            _consumer.unsubscribe();
            _processMessagesTimer.stop();
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
         * queue or a gap is found in the id's of the messages in the queue.
         *
         * Note that CHAT messages have no id and are always processed.
         */
        private function processQueue():void{
            _messages.sort(compare);
            var msg:IMessage;
            var now:Date = new Date();
            while(_messages.length > 0){
                msg = IMessage(_messages[0]);
                if (msg.headers.GAME_EVENT == GameEvent.CHAT || _lastMsgId == 0 || msg.headers.MESSAGE_ID > _lastMsgId){
                    if (msg.headers.GAME_EVENT == GameEvent.MOVE_COMPLETE){
                        if (_lastMoveCompleted != null && (now.getTime() - _lastMoveCompleted.getTime()) < MINIMUM_MOVE_TIME){

                            //if timer not already running then start
                            if(!_processMessagesTimer.running){
                                _processMessagesTimer.reset();
                                _processMessagesTimer.start();
                            }
                            break;
                        }
                        _lastMoveCompleted = now;
                    }
                    if(msg.headers.MESSAGE_ID != 0){
                        _lastMsgId = msg.headers.MESSAGE_ID;
                    }
                    trace("processing message with id " + msg.headers.MESSAGE_ID + "\n Game event = " + msg.headers.GAME_EVENT, "Processing message");
                    var evt:DynamicEvent = new DynamicEvent(PROCESS_MESSAGE);
                    evt.message = msg;
                    dispatchEvent(evt);
                    _messages.splice(0, 1);
                }
            }
        }

        private function handleTimer(event:TimerEvent):void{
            _processMessagesTimer.stop();
            processQueue();
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
