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
                    <Route exact path="/console/editor/:path" component={Editor}/>
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

    isActive(route) {
        const pathname = this.state.pathname || history.location.pathname;
        return route && pathname.startsWith(route);
    }

    isItemActive(route) {
        const pathname = this.state.pathname || history.location.pathname;
        return route && pathname === route;
    }

    toggleMenu() {
        this.setState({shownMenu: !this.state.shownMenu}, () => this.fixPage());
    }

    fixMenu() {
        this.setState({fixedMenu: !this.state.fixedMenu}, () => this.fixPage());
    }

    fixPage(fixed) {
        this.setState({pageFixed: this.state.shownMenu && this.state.fixedMenu});
    }

    render() {
        return (
            <div>
                <Menu theme="dark" defaultActive="1" className="el-menu-demo" mode="horizontal" onSelect={index => this.onSelect(index)}>
                    <Menu.Item index="/repo">Repo</Menu.Item>
                </Menu>
                <Page/>
            </div>
        );
    }

    onSelect(index) {
        if (index.startsWith("/")) {
            history.push(index);
        }
    }
}

ReactDOM.render(<App/>, document.getElementById("app"));

