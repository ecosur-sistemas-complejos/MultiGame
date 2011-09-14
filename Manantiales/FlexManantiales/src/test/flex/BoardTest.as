package {

import mx.core.FlexGlobals;
import mx.events.FlexEvent;
import flexunit.framework.TestCase;
import mx.ecosur.multigame.manantiales.ManantialesBoard;

public class BoardTest extends TestCase {

        private var _board:ManantialesBoard;

        override public function tearDown():void
        {
            try {
                FlexGlobals.topLevelApplication.removeChild(_board);
            } catch (argumentError:ArgumentError) { }

            _board = null;
        }

        [Test]
        public function testDrawBoard():void {
            _board = new ManantialesBoard();
            _board.addEventListener(FlexEvent.CREATION_COMPLETE, addAsync(verifyBoard, 1000));
            FlexGlobals.topLevelApplication.addChild(_board);
        }

        public function verifyBoard(event:FlexEvent):void {
            assertTrue(_board.initialized);
        }
    }

}
