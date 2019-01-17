import React from "react";
import PropTypes from "prop-types";
import {Breadcrumb, Button, Card, Layout} from "element-react";
import {Link} from "react-router-dom";
import AceEditor from "react-ace";

import "brace";
import "brace/mode/java";
import "brace/mode/javascript";
import "brace/mode/html";
import "brace/mode/css";
import "brace/theme/github";

const availableModes = ["java", "javascript", "html", "css"];

export default class Editor extends React.Component {
    constructor(props) {
        super(props);
        const {repositoryName, branch, name, isNew} = props.location.state;
        const nameArray = name.split(".");
        const fileExt = nameArray[nameArray.length - 1];
        let mode = "text";
        if (fileExt && availableModes.indexOf(fileExt.toLocaleLowerCase()) >= 0) {
            mode = fileExt;
        }
        const path = props.location.pathname.replace("/console/editor", "");

        this.state = {
            path,
            repositoryName,
            branch,
            isNew,
            name,
            content: "",
            mode,
            folders: []
        };
    }

    componentDidMount() {
        if (this.state.path) {
            this.changeFolder(this.state.path);
        }
        if (!this.state.isNew) {
            fetch(`/api/repository/file/${this.state.repositoryName}/${this.state.branch}/${this.state.path}`).then((data) => {
                this.setState({content: data.content});
            });
        }
    }

    changeFolder(path) {
        const paths = path.split("/");
        let newPath = "";
        const folders = [];
        paths.forEach((p) => {
            if (p) {
                newPath += path + "/";
                folders.push(newPath);
            }
        });
        this.setState({
            path,
            folders
        });
    }

    onChange(content) {
        this.setState({content});
    }

    submit() {
        fetch("/", {
            path: this.state.path,
            repositoryName: this.state.repositoryName,
            content: this.state.content
        }).then((data) => {
            this.props.history.push(`/console/repo/${this.state.repositoryName}`);
        });
    }

    render() {
        return (
            <div>
                <Layout.Row>
                    <Layout.Col span="16">
                        <Breadcrumb separator="/">
                            <Breadcrumb.Item>
                                <Link to="/console">Home</Link>
                            </Breadcrumb.Item>
                            <Breadcrumb.Item>
                                <Link to={`/console/repo/${this.state.repositoryName}`}>{this.state.repositoryName}</Link>
                            </Breadcrumb.Item>
                            {this.state.folders.map(folder =>
                                <Breadcrumb.Item key={folder}>
                                    <Link to={`/console/repo/${this.state.repositoryName}/${folder}`}>{this.state.repositoryName}</Link>
                                </Breadcrumb.Item>)

                            }


                        </Breadcrumb>
                    </Layout.Col>
                    <Layout.Col span="8">
                        <div className="head-operation">
                            <Button class="primary" size="small" onClick={() => this.submit()}>Save </Button>
                        </div>
                    </Layout.Col>
                </Layout.Row>
                <Layout.Row>
                    <Layout.Col span="24">
                        <Card>
                            <AceEditor
                                mode={this.state.mode}
                                theme="github"
                                value={this.state.content}
                                onChange={value => this.onChange(value)}
                                name="UNIQUE_ID_OF_DIV"
                                editorProps={{$blockScrolling: true}}
                            />
                        </Card>
                    </Layout.Col>
                </Layout.Row>
            </div>
        );
    }

}

Editor.propTypes = {
    history: PropTypes.object,
    location: PropTypes.object,
    match: PropTypes.object
};
