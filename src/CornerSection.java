
/***********************************************************************
 *   RacingGame 1.0, a java networked racing game
 *   Copyright (C) 2001  John S Montgomery (john.montgomery@lineone.net)
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ************************************************************************/

import java.awt.Rectangle;

public class CornerSection extends Track.Section {
	private boolean left; // left edge open
	private boolean top;  // top edge open
	private int corner_x, corner_y;

	public CornerSection( boolean left, boolean top, int x, int y ) {
		super( x, y );
		this.left = left;
		this.top = top;
		if ( left ) 
			corner_x = x;
		else
			corner_x = x + getBounds().width;

		if ( top )
			corner_y = y;
		else
			corner_y = y + getBounds().height;
	}

	public int type() {
		return CORNER;
	}

	public float distanceToEnd( Vehicle vehicle ) {
		Track.Section next = getNextSection();
		Rectangle bounds = next.getBounds();
		boolean horiz = bounds.x != getBounds().x;
		Body.State state = vehicle.currentState();
		if ( horiz ) {
			float dist = corner_x - state.x;
			if ( dist < 0 ) dist = -dist;
			return dist;
		}
		else {
			float dist = corner_y - state.y;
			if ( dist < 0 ) dist = -dist;
			return dist;
		}
	}

	public boolean toLeft() {
		return left;
	}

	public boolean toTop() {
		return top;
	}

	public int cornerX() {
		return corner_x;
	}

	public int cornerY() {
		return corner_y;
	}

	public boolean intersects( Vehicle vehicle, Body.State state ) {
		Rectangle bounds = getBounds();
		for ( int i = 0; i < state.numpoints; i++ ) {
			float x = state.xpoints[ i ];
			float y = state.ypoints[ i ];
			if ( bounds.contains( (int)x, (int)y ) ) {
				return true;
			}
		}
		return false;
	}

	public void checkCollision( Vehicle vehicle, Body.State state ) {
		
		Rectangle bounds = getBounds();
			
		/*for ( int i = 0; i < state.numpoints; i++ ) {
			float x = state.xpoints[ i ];
			float y = state.ypoints[ i ];
			if ( !left && x <= bounds.x ) {
				state.collide( x, y, 1, 0 );
			}
			if ( left && x >= (bounds.x + bounds.width) ) {
				state.collide( x, y, -1, 0 );
			}
		}		

		for ( int i = 0; i < state.numpoints; i++ ) {
			float x = state.xpoints[ i ];
			float y = state.ypoints[ i ];
			if ( !top && y <= bounds.y ) {
				state.collide( x, y, 0, 1 );
			}
			if ( top && y >= (bounds.y + bounds.height) ) {
				state.collide( x, y, 0, -1 );
			}
		}*/

		for ( int i = 0; i < state.numpoints; i++ ) {
			float x = state.xpoints[ i ];
			float y = state.ypoints[ i ];
			float rx = x - corner_x,
				  ry = y - corner_y;
			
			//if ( !bounds.contains( (int)x, (int)y ) )
			//
			if ( top && y < bounds.y )
				  continue;
			if ( !top && y > bounds.y + bounds.height )
				  continue;
			if ( left && x < bounds.x )
				  continue;
			if ( !left && x > bounds.x + bounds.width )
				  continue;
			  
			float distSq = rx*rx + ry*ry;
			if ( distSq > bounds.width*bounds.width ) {
				float dist = (float)Math.sqrt( distSq );
				rx /= dist;
				ry /= dist;
				state.collide( x, y, -rx, -ry );
			}
		}
		
	}

}
