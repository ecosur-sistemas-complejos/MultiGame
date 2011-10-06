package {

import mx.core.FlexGlobals;
import mx.events.FlexEvent;

import flexunit.framework.TestCase;

import mx.ecosur.multigame.manantiales.ManantialesBoard;

public class BoardTest extends TestCase {

    private var board:ManantialesBoard;

    [Before]
    public override function setUp():void {
        super.setUp();
        board = new ManantialesBoard();
        board.initialize();
        FlexGlobals.topLevelApplication.addChild(board);
    }

    [Test]
    public function testBoard():void {
        assertTrue(board != null);
    }

}

}
