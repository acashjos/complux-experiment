require=(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require("react");

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Modal = function (_Component) {
  _inherits(Modal, _Component);

  function Modal() {
    _classCallCheck(this, Modal);

    return _possibleConstructorReturn(this, (Modal.__proto__ || Object.getPrototypeOf(Modal)).apply(this, arguments));
  }

  _createClass(Modal, [{
    key: "inputChange",
    value: function inputChange(e) {
      this.value = e.target.value;
    }
  }, {
    key: "show",
    value: function show() {
      $("#myModal").modal("show");
    }
  }, {
    key: "render",
    value: function render() {
      var _this2 = this;

      return _react2.default.createElement(
        "div",
        { id: "myModal", className: "modal fade", role: "dialog" },
        _react2.default.createElement(
          "div",
          { className: "modal-dialog" },
          _react2.default.createElement(
            "div",
            { className: "modal-content" },
            _react2.default.createElement(
              "div",
              { className: "modal-header" },
              _react2.default.createElement(
                "button",
                { type: "button", className: "close", "data-dismiss": "modal" },
                "\xD7"
              ),
              _react2.default.createElement(
                "h4",
                { className: "modal-title" },
                "New todo item"
              )
            ),
            _react2.default.createElement(
              "div",
              { className: "modal-body" },
              _react2.default.createElement("input", { onChange: this.inputChange.bind(this) })
            ),
            _react2.default.createElement(
              "div",
              { className: "modal-footer" },
              _react2.default.createElement(
                "button",
                { type: "button", className: "btn btn-default", "data-dismiss": "modal" },
                "Close"
              ),
              _react2.default.createElement(
                "button",
                { type: "button", className: "btn btn-success", "data-dismiss": "modal",
                  onClick: function onClick() {
                    _this2.props.onSuccess(_this2.value);
                  } },
                "Add"
              )
            )
          )
        )
      );
    }
  }]);

  return Modal;
}(_react.Component);

exports.default = Modal;

},{"react":"react"}],"/src/App/Main.js":[function(require,module,exports){
'use strict';

Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Modal = require('./Modal');

var _Modal2 = _interopRequireDefault(_Modal);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }
// import './Todo.css';


var Main = function (_Component) {
	_inherits(Main, _Component);

	function Main(props) {
		_classCallCheck(this, Main);

		var _this = _possibleConstructorReturn(this, (Main.__proto__ || Object.getPrototypeOf(Main)).call(this, props));

		_this.state = {
			taskList: []
		};
		return _this;
	}

	_createClass(Main, [{
		key: 'componentWillReceiveProps',
		value: function componentWillReceiveProps(props) {
			this.setState(props.appState);
		}
	}, {
		key: 'render',
		value: function render() {
			var _this2 = this;

			var Head = this.props.Head || function () {
				return false;
			};

			return _react2.default.createElement(
				'div',
				null,
				_react2.default.createElement(
					Head,
					null,
					_react2.default.createElement('meta', { charSet: 'utf-8' }),
					_react2.default.createElement(
						'title',
						null,
						'Todo'
					),
					_react2.default.createElement('link', { rel: 'stylesheet', href: 'https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css' })
				),
				_react2.default.createElement('br', null),
				_react2.default.createElement('br', null),
				_react2.default.createElement(
					'nav',
					{ className: 'navbar navbar-inverse navbar-fixed-top' },
					_react2.default.createElement(
						'div',
						{ className: 'container-fluid' },
						_react2.default.createElement(
							'div',
							{ className: 'navbar-header' },
							_react2.default.createElement(
								'button',
								{ type: 'button', className: 'navbar-toggle collapsed', 'data-toggle': 'collapse', 'data-target': '#navbar', 'aria-expanded': 'false',
									'aria-controls': 'navbar' },
								_react2.default.createElement(
									'span',
									{ className: 'sr-only' },
									'Toggle navigation'
								),
								_react2.default.createElement('span', { className: 'icon-bar' }),
								_react2.default.createElement('span', { className: 'icon-bar' }),
								_react2.default.createElement('span', { className: 'icon-bar' })
							),
							_react2.default.createElement(
								'a',
								{ className: 'navbar-brand', href: '#' },
								'ToDo - Desktop'
							)
						),
						_react2.default.createElement(
							'div',
							{ id: 'navbar', className: 'navbar-collapse collapse' },
							_react2.default.createElement(
								'ul',
								{ className: 'nav navbar-nav navbar-right' },
								_react2.default.createElement(
									'li',
									{ onClick: function onClick() {
											_this2.modal.show();
										} },
									_react2.default.createElement(
										'a',
										{ href: '#' },
										_react2.default.createElement('i', { className: 'fa fa-plus', 'aria-hidden': 'true' }),
										' Add'
									)
								)
							)
						)
					)
				),
				_react2.default.createElement(
					'div',
					{ className: 'container-fluid' },
					_react2.default.createElement(
						'div',
						{ className: 'row' },
						_react2.default.createElement(
							'div',
							{ className: 'col-sm-3 col-md-2 sidebar' },
							_react2.default.createElement(
								'ul',
								{ className: 'nav nav-sidebar' },
								_react2.default.createElement(
									'li',
									{ className: 'active' },
									_react2.default.createElement(
										'a',
										{ href: '#' },
										'Overview ',
										_react2.default.createElement(
											'span',
											{ className: 'sr-only' },
											'(current)'
										)
									)
								),
								_react2.default.createElement(
									'li',
									null,
									_react2.default.createElement(
										'a',
										{ href: '#' },
										'Reports'
									)
								)
							),
							_react2.default.createElement(
								'ul',
								{ className: 'nav nav-sidebar' },
								_react2.default.createElement(
									'li',
									null,
									_react2.default.createElement(
										'a',
										{ href: '' },
										'Nav item'
									)
								)
							),
							_react2.default.createElement(
								'ul',
								{ className: 'nav nav-sidebar' },
								_react2.default.createElement(
									'li',
									null,
									_react2.default.createElement(
										'a',
										{ href: '' },
										'Nav item again'
									)
								)
							)
						),
						_react2.default.createElement(
							'div',
							{ className: 'col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main' },
							_react2.default.createElement(
								'h1',
								{ className: 'page-header' },
								'Todo List'
							),
							_react2.default.createElement(
								'div',
								{ className: 'table-responsive' },
								_react2.default.createElement(
									'table',
									{ className: 'table table-striped' },
									_react2.default.createElement(
										'thead',
										null,
										_react2.default.createElement(
											'tr',
											null,
											_react2.default.createElement(
												'th',
												null,
												'#'
											),
											_react2.default.createElement(
												'th',
												{ colSpan: '3' },
												'Item'
											),
											_react2.default.createElement(
												'th',
												null,
												'*'
											)
										)
									),
									_react2.default.createElement(
										'tbody',
										null,
										this.state.taskList.map(function (task, i) {
											return _react2.default.createElement(
												'tr',
												{ key: i },
												_react2.default.createElement(
													'td',
													null,
													i + 1
												),
												_react2.default.createElement(
													'td',
													{ colSpan: '3' },
													task
												),
												_react2.default.createElement(
													'td',
													null,
													_react2.default.createElement(
														'button',
														{ onClick: function onClick() {
																return _this2.props.updateState('deleteTask', task);
															} },
														'Delete'
													)
												)
											);
										})
									)
								)
							)
						)
					)
				),
				_react2.default.createElement(_Modal2.default, { ref: function ref(_ref) {
						return _this2.modal = _ref;
					}, onSuccess: function onSuccess(val) {
						return _this2.props.updateState('addTask', val);
					} })
			);
		}
	}]);

	return Main;
}(_react.Component);

exports.default = Main;

},{"./Modal":1,"react":"react"}]},{},[]);
