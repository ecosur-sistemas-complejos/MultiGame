/**
 * A simple, point-based, adjacency graph
 *
 * @author: awaterma@ecosur.mx
 */
package mx.ecosur.multigame.manantiales {
import flash.geom.Point;
import flash.utils.Dictionary;

public class AdjGraph {

        var E:int,  V:int;

        var adj:Array;

        var dict:Dictionary;

        public function AdjGraph(V:int) {
            this.V = V;
            init();
        }

        private function init():void {
            dict = new Dictionary();
            adj = new Array(V);
            for (var i:int = 0; i < V; i++) {
                adj [ i ] = new Array();
                for (var j:int = 0; j < V; j++) {
                    adj [ i ][ j ] = false;
                }
            }
        }

        public function addEdge (v:int,  w:int):void {
            if (!adj[v][w]) E++;
            adj[v][w] = true;
            adj[w][v] = true;
        }

        public function containsEdge (v:int,  w:int):Boolean {
            return (adj [v][w]);
        }

        public function findPoint(node:int):Point {
           return dict[node];
        }

        public function addPoint(node:int,  point:Point):void {
            dict[node] = point;
        }

        public function getNodes():Array {
            var ret:Array = new Array();
            for (var obj:Object in dict) {
                    ret.push(obj);
            }

            return ret;
        }
    }
}
