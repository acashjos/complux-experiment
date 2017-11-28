import React, { Component } from 'react';

import { Helmet } from "react-helmet";

class Container extends Component {

  constructor(props) {
    super(props);
    this.updateState = this.updateState.bind(this);
    this.state = {}
  }
  componentDidMount() {
    this.socket = window.io.connect('', {
      query: 'app=' + (window.location.pathname.match(/^\/?([^\/]+)/) || [])[1]
    });

    this.socket.on('disconnected', () => {
      this.setState({ disconnected: true })
    })

    this.socket.on('stateChange', (payload) => {
      this.setState(payload); // payload should contain { context: string , state: Object }
    })
  }

  updateState(action, params) {
    let obj = { action, params }
    this.socket.emit("relay", obj);
    // this.setState({
    //   state: {
    //     taskList: [params]
    //   }
    // });
  }

  render() {
    let Main = window.Main.component
    console.log(this.state, Main)
    return (
      this.state.disconnected ? (<p>Phone disconnected</p>) : <Main
        Head={Helmet}
        updateState={this.updateState}
        context={this.state.context}
        appState={this.state.state}
      />
    );
  }
}

export default Container;
