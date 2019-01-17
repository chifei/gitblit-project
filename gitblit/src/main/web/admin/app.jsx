import "babel-polyfill";
import React from "react";
import ReactDOM from "react-dom";
import {Route, Router, Switch} from "react-router-dom";
import createHistory from "history/createBrowserHistory";
import {Menu} from "element-react";
import "./lib/fetch";

import RepoList from "./repository-list";
import Repository from "./repository/repository";
import Editor from "./editor/editor";

import "./console.html";
import "element-theme-default";
import "./main.css";

const history = createHistory({basename: "/"});

class Page extends React.Component {
    constructor(props) {
        super(props);
        this.state = {};
    }

    shouldComponentUpdate() {
        return false;
    }

    render() {
        return (
            <Router history={history}>
                <Switch>
                    <Route exact path="/console/" component={RepoList}/>
                    <Route exact path="/console/repo/:repositoryName" component={Repository}/>
                    <Route path="/console/repo/:repositoryName/:path" component={Repository}/>
                    <Route path="/console/editor/:path" component={Editor}/>
                    <Route component={RepoList}/>
                </Switch>
            </Router>
        );
    }
}

class App extends React.Component {
    constructor(props) {
        super(props);
        const defaultOpeneds = [];
        this.state = {
            defaultOpeneds: defaultOpeneds,
            pathname: history.location.pathname,
            pageFixed: true,
            shownMenu: true,
            fixedMenu: true
        };
        history.listen((location) => {
            this.setState({pathname: location.pathname});
        });
    }

    render() {
        return (
            <div className="app">
                <div className="header">
                    <Menu theme="dark" defaultActive="1" className="el-menu-demo" mode="horizontal" onSelect={index => this.onSelect(index)}>
                        <Menu.Item index="/console">Console</Menu.Item>
                    </Menu>
                </div>
                <Page/>
            </div>
        );
    }

    onSelect(index) {
        history.push(index);
    }
}

ReactDOM.render(<App/>, document.getElementById("app"));

