/**
 * A simple, point-based, adjacency graph
 *
 * @author: awaterma@ecosur.mx
 */
package mx.ecosur.multigame.manantiales {

    import flash.geom.Point;

    public class AdjGraph {

        private var E:int,  V:int, adj:Array, dict:Array;

        public function AdjGraph(V:int) {
            this.V = V;
            init();
        }

        private function init():void {
            dict = new Array(V);
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

        public function setPoint(node:int,  point:Point):void {
            dict[node] = point;
        }

        public function getNodes():Array {
            return dict;
        }
    }
}
