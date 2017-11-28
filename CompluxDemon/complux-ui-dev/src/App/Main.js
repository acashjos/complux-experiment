import React, { Component } from 'react';
// import './Todo.css';
import Modal from './Modal';

class Main extends Component {

	constructor(props) {
		super(props)
		this.state = {
			taskList: []
		}
	}
	componentWillReceiveProps(props) {
		this.setState(props.appState);
	}
	render() {
		let Head = this.props.Head || (() => false);

		return (
			<div>
				<Head>
					<meta charSet="utf-8" />
					<title>Todo</title>
					<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" />
				</Head>

				<br />
				<br />
				<nav className="navbar navbar-inverse navbar-fixed-top">
					<div className="container-fluid">
						<div className="navbar-header">
							<button type="button" className="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false"
								aria-controls="navbar">
								<span className="sr-only">Toggle navigation</span>
								<span className="icon-bar"></span>
								<span className="icon-bar"></span>
								<span className="icon-bar"></span>
							</button>
							<a className="navbar-brand" href="#">ToDo - Desktop</a>
						</div>
						<div id="navbar" className="navbar-collapse collapse">
							<ul className="nav navbar-nav navbar-right">
								<li onClick={() => { this.modal.show() }}>
									<a href="#"><i className="fa fa-plus" aria-hidden="true"></i> Add</a>
								</li>
							</ul>
							{/*<form className="navbar-form navbar-right">
								<input type="text" className="form-control" placeholder="Search..."/>
							</form>*/}
						</div>
					</div>
				</nav>
				<div className="container-fluid">
					<div className="row">
						<div className="col-sm-3 col-md-2 sidebar">
							<ul className="nav nav-sidebar">
								<li className="active"><a href="#">Overview <span className="sr-only">(current)</span></a></li>
								<li><a href="#">Reports</a></li>
							</ul>
							<ul className="nav nav-sidebar">
								<li><a href="">Nav item</a></li>
							</ul>
							<ul className="nav nav-sidebar">
								<li><a href="">Nav item again</a></li>
							</ul>
						</div>
						<div className="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
							<h1 className="page-header">Todo List</h1>

							<div className="table-responsive">
								<table className="table table-striped">
									<thead>
										<tr>
											<th>#</th>
											<th colSpan="3">Item</th>
											<th>*</th>
										</tr>
									</thead>
									<tbody>
										{this.state.taskList.map((task,i) => (
											<tr key={i}>
												<td>{i+1}</td>
												<td colSpan="3">{task}</td>
												<td><button onClick={()=> this.props.updateState('deleteTask',task)}>Delete</button></td>
											</tr>
										))
										}
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
				<Modal ref={ref => this.modal = ref} onSuccess={val => this.props.updateState('addTask', val)} />
			</div>
		);
	}
}

export default Main;