/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 10/5/11
 * Time: 12:40 PM
 * To change this template use File | Settings | File Templates.
 */
package {
import flash.geom.Point;

import flexunit.framework.TestCase;

import mx.ecosur.multigame.manantiales.AdjGraph;

public class AdjGraphTest extends TestCase {

    var graph:AdjGraph;

    [Before]
    public override function setUp():void {
        super.setUp();
        graph = new AdjGraph(5);
        graph.setPoint(0, new Point(0, 0));
        graph.setPoint(1, new Point(0, 2));
        graph.setPoint(2, new Point(1, 1));
        graph.setPoint(3, new Point(2, 0));
        graph.setPoint(4, new Point(2, 2));

        graph.addEdge(0,1);
        graph.addEdge(0,2);
        graph.addEdge(0,3);
        graph.addEdge(1,2);
        graph.addEdge(1,4);
        graph.addEdge(2,3);
        graph.addEdge(2,4);
        graph.addEdge(3,4);
    }

    [Test]
    public function testGetNodes():void {
        var n:Array = graph.getNodes();
        assertTrue("Incorrect length: " + n.length, n.length == 5);
    }

    [Test]
    public function testFindPoint():void {
        var nodes:Array = graph.getNodes();
        assertTrue(nodes [0].equals(new Point (0,0)));
        assertTrue(nodes [1].equals(new Point (0,2)));
        assertTrue(nodes [2].equals(new Point (1,1)));
        assertTrue(nodes [3].equals(new Point (2,0)));
        assertTrue(nodes [4].equals(new Point (2,2)));
    }

    [Test]
    public function testEdges():void {
        assertTrue(graph.containsEdge(0,1));
        assertTrue(graph.containsEdge(1,0));
        assertTrue(graph.containsEdge(0,3));
        assertTrue(graph.containsEdge(3,0));
        assertTrue(graph.containsEdge(0,2));
        assertTrue(graph.containsEdge(2,0));
        assertTrue(graph.containsEdge(2,3));
        assertTrue(graph.containsEdge(3,2));
        assertTrue(graph.containsEdge(2,4));
        assertTrue(graph.containsEdge(4,2));

        /* Negative assertions */

        assertTrue(!graph.containsEdge(4,0));
        assertTrue(!graph.containsEdge(0,4));
        assertTrue(!graph.containsEdge(3,1));
        assertTrue(!graph.containsEdge(1,3));
    }
}
}
