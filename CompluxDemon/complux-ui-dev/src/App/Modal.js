import React, { Component } from 'react';

class Modal extends Component {

	inputChange(e){
		this.value = e.target.value
	}
	show(){
		$("#myModal").modal("show") ;
	}
  render() {
    return (
      
<div id="myModal" className="modal fade" role="dialog">
  <div className="modal-dialog">

    <div className="modal-content">
      <div className="modal-header">
        <button type="button" className="close" data-dismiss="modal">&times;</button>
        <h4 className="modal-title">New todo item</h4>
      </div>
      <div className="modal-body">
        <input onChange={this.inputChange.bind(this)}/>
      </div>

		<div className="modal-footer">
			<button type="button" className="btn btn-default" data-dismiss="modal">Close</button>
			<button type="button" className="btn btn-success" data-dismiss="modal" 
			onClick={()=>{this.props.onSuccess(this.value)}}>Add</button>
		</div>
    </div>

  </div>
</div>
    );
  }
}

export default Modal;
