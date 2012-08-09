/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.gente.entity {
	
	import mx.collections.ArrayCollection;
	import mx.ecosur.multigame.entity.GamePlayer;
	
	/**
	 * Represents a server side GamePlayer object
	 */
	[RemoteClass (alias="mx.ecosur.multigame.gente.entity.GentePlayer")]
	public class GentePlayer extends GamePlayer{
		
		private var _points:int;
		private var _trias:ArrayCollection;
		private var _tesseras:ArrayCollection;
		private var _partner:GentePlayer;
		
		public function GentePlayer(){
			super();
		}
		
		public function get points():int{
			return _points;
		}
		
		public function set points(points:int):void {
			_points = points;
		}
		
		public function get trias():ArrayCollection{
			return _trias;
		}
		
		public function set trias(trias:ArrayCollection):void {
			_trias = trias;
		}
		
		public function get tesseras():ArrayCollection{
			return _tesseras;
		}
		
		public function set tesseras(tesseras:ArrayCollection):void {
			_tesseras = tesseras;
		}
		
		public function get partner():GentePlayer{
			return _partner;
		}
		
		public function set partner(partner:GentePlayer):void {
			_partner = partner;
		}
	}
}
